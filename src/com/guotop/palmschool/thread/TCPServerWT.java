package com.guotop.palmschool.thread;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guotop.palmschool.check.cons.CheckCons;
import com.guotop.palmschool.entity.Inout;
import com.guotop.palmschool.listener.DBContextHolder;
import com.guotop.palmschool.listener.StartupListener;
import com.guotop.palmschool.util.StringUtil;

/**
 * UDP接收端
 * 
 * 对应上海秀派读卡器 接收读卡器发送过来的数据
 * 现在【文婷实验学校】【蕉城区实验学校】【宁德附小】正在使用
 * 
 */
public class TCPServerWT extends Thread
{

	private Logger log = LoggerFactory.getLogger(TCPServerWT.class);

	private static ServerSocket server = null;

	protected final static int PORT = 13121;

	protected final static int BeginP = 9;

	private static boolean signal = true;

	private Long schoolId;

	/**
	 * 注入配置项 0:使用ip地址 1:使用设备号
	 */
	private String ipSwitch;

	private static List<SocketHandle> sockets = new ArrayList<SocketHandle>();

	public TCPServerWT()
	{
		if (server == null)
		{
			try
			{
				server = new ServerSocket(PORT);
				server.setSoTimeout(1000);
				this.start();
			} catch (Exception e)
			{
				log.error("启动TCPServerWT失败", e);
				e.printStackTrace();
			}
		}
	}

	public void run()
	{
		log.info("TCPServerWT启动");
		while (server != null && signal)
		{
			Socket socket = null;
			try
			{
				socket = server.accept();
			} catch (Exception e)
			{
			}
			if (socket != null)
			{
				SocketHandle sh = new SocketHandle(socket);
				sockets.add(sh);
			}
		}
		log.info("TCPServerWT结束");
	}

	public void disconnect()
	{
		signal = false;
		this.interrupt();
		try
		{
			for (SocketHandle s : sockets)
			{
				s.close();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			server.close();
			server = null;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void trunInOut(String deviceNo, String cardNo, int inOut, String ip,String time)
	{
		// byte[] cardbytes = new byte[4];
		// System.arraycopy(cardNo, 0, cardbytes, 0, 4);
		// String code = new String(cardbytes); // 卡号
		//
		int battery = 1; // 电池状态，1正常，0电压偏低
		// int status = cardNo[11] - 48; // 状态，1进入，0外出

//		log.info("get:" + cardNo + " [" + cardNo + "][" + battery + "][" + inOut + "][" + ip + "][deviceIDNo：" + deviceNo + "]");

		// ipSwitch 0:IP 1:deviceCode 设备号
		ipSwitch = "1";

		if (cardNo.length() == 9)// 如果卡号是九位则前面加0
		{
			cardNo = "0" + cardNo;
		}

		Inout inout = new Inout();
		inout.setCode(cardNo);
		if (inOut == 0)// 出
		{
			inOut = 2;
		}
		inout.setStatus(inOut);
		inout.setCreateTime(time);
		String position = "";

		if (CheckCons.IPSWITCH_DEVICECODE.equals(ipSwitch))
		{
			// 记录设备号
			position = deviceNo;
		} else
		{
			// 记录ip
			position = ip;
		}

		schoolId = StartupListener.deviceService.getSchoolIdByDeviceCode(deviceNo);
		if (null != schoolId && 0l != schoolId)
		{
			// 根据设备查找数据源
			DBContextHolder.setDBType(String.valueOf(schoolId));
			//作用是，打卡超过10min之后才发来的数据只保存到inout表中，**【不推送不短信】**
			boolean isSendSMS = true;
			if(!StringUtil.isEmpty(inout.getCreateTime())){
				try{
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					long inoutMillionSeconds = formatter.parse(inout.getCreateTime()).getTime();//毫秒
					long currentMillionSeconds = System.currentTimeMillis();
					if(currentMillionSeconds - Long.valueOf(inoutMillionSeconds) >= 1000*60*10 ){
						isSendSMS = false;
					}
				}catch(Exception e){
					isSendSMS = false;
					log.error("TCPServerWT,解析时间出错,错误信息如下:"+e.getMessage());
				}
			}
			StartupListener.inoutService.addInout(inout, ipSwitch, position,isSendSMS);
		} else
		{
			log.info("deviceIDNo:" + deviceNo + "未找到对应的学校！");
		}

	}

	private void trunTagAlarm(String cardNo, String ip)
	{
		// byte[] cardbytes = new byte[10];
		// System.arraycopy(cardNo, 0, cardbytes, 0, 10);
		// String code = new String(cardbytes); // 卡号

		int battery = 0; // 电池状态，1正常，0电压偏低
		int status = 2; // 状态，1进入，0外出,2电压报警

		log.info("get:" + cardNo + " [" + cardNo + "][" + battery + "][" + status + "][" + ip + "]");

		Inout inout = new Inout();
		inout.setCode(cardNo);
		inout.setStatus(status);
		inout.setPosition(ip);
		// StartupListener.add2CheckList(inout);

	}

	class SocketHandle extends Thread
	{
		private Socket socket = null;
		private InputStream in = null;
		private OutputStream out = null;
		private String ip = null;
		// private CRC16 crc16 = new CRC16();

		public SocketHandle(Socket socket)
		{
			this.socket = socket;
			this.ip = socket.getInetAddress().toString();
			this.start();
		}

		public int byteToInt(byte b)
		{
			int i = (int) b;
			if (i >= 0)
			{
				return i;
			} else
			{
				return 256 + i;
			}
		}

		private byte[] TimeSyncCRC(boolean CRCOk, byte[] messgeData)
		{
			byte[] data = new byte[0x0F - 2];
			System.arraycopy(messgeData, 0, data, 0, data.length);

			// 帧选项
			int pos = 0;// 启始从帧选项
			int fieldLen = 1;// 数据项长度
			data[pos] = 0x40;// 上位机到前置机帧、点对点帧
			pos = pos + fieldLen;

			// 协议代码//保持不变
			fieldLen = 2;
			pos = pos + fieldLen;

			// 命令代码//保持不变
			fieldLen = 1;
			pos = pos + fieldLen;

			// 帧序号//保持不变
			fieldLen = 2;
			pos = pos + fieldLen;

			// //和终端论证比，没有状态
			// data[pos]= (byte) (CRCOk ? 0x0: 0x1);//终端认证状态符，其中：00h – 认证成功，01h
			// – 认证失败
			// fieldLen = 1;
			// pos = pos + fieldLen;

			// 时间
			fieldLen = 7;
			Calendar c = Calendar.getInstance();

			int year = c.get(Calendar.YEAR);
			byte[] yearBytes = CodecUtil.short2bytes((short) year);
			data[pos] = yearBytes[0];
			data[pos + 1] = yearBytes[1];

			int month = c.get(Calendar.MONTH);
			data[pos + 2] = (byte) (month + 1);

			int date = c.get(Calendar.DATE);
			data[pos + 3] = (byte) date;

			int hour = c.get(Calendar.HOUR_OF_DAY);
			data[pos + 4] = (byte) hour;

			int minute = c.get(Calendar.MINUTE);
			data[pos + 5] = (byte) minute;

			int second = c.get(Calendar.SECOND);
			data[pos + 6] = (byte) second;

			// log.info("get:TimeSyncCRC" + StringUtil.toHex(data) + "信息"+
			// CRCOk);
			return data;
		}

		private byte[] terAuthenCRC(boolean CRCOk, byte[] messgeData)
		{
			byte[] data = new byte[0x10 - 2];
			System.arraycopy(messgeData, 0, data, 0, data.length);

			// 帧选项
			int pos = 0;// 启始从帧选项
			int fieldLen = 1;// 数据项长度
			data[pos] = 0x40;// 上位机到前置机帧、点对点帧
			pos = pos + fieldLen;

			// 协议代码//保持不变
			fieldLen = 2;
			pos = pos + fieldLen;

			// 命令代码//保持不变
			fieldLen = 1;
			pos = pos + fieldLen;

			// 帧序号//保持不变
			fieldLen = 2;
			pos = pos + fieldLen;

			// 状态
			data[pos] = (byte) (CRCOk ? 0x0 : 0x1);// 终端认证状态符，其中：00h – 认证成功，01h
													// – 认证失败
			fieldLen = 1;
			pos = pos + fieldLen;

			// 时间
			fieldLen = 7;
			Calendar c = Calendar.getInstance();

			int year = c.get(Calendar.YEAR);
			byte[] yearBytes = CodecUtil.short2bytes((short) year);
			data[pos] = yearBytes[0];
			data[pos + 1] = yearBytes[1];

			int month = c.get(Calendar.MONTH);
			data[pos + 2] = (byte) (month + 1);

			int date = c.get(Calendar.DATE);
			data[pos + 3] = (byte) date;

			int hour = c.get(Calendar.HOUR_OF_DAY);
			data[pos + 4] = (byte) hour;

			int minute = c.get(Calendar.MINUTE);
			data[pos + 5] = (byte) minute;

			int second = c.get(Calendar.SECOND);
			data[pos + 6] = (byte) second;

			// log.info("get:terAuthenCRC" + StringUtil.toHex(data) + "信息"+
			// CRCOk);
			return data;
		}

		private byte[] UplAttRecsCRC71To74(boolean CRCOk, byte[] messgeData)
		{
			byte[] data = new byte[0x09 - 2];
			System.arraycopy(messgeData, 0, data, 0, data.length);

			// 帧选项
			int pos = 0;// 启始从帧选项
			int fieldLen = 1;// 数据项长度
			data[pos] = 0x40;// 上位机到前置机帧、点对点帧
			pos = pos + fieldLen;

			// 协议代码//保持不变
			fieldLen = 2;
			pos = pos + fieldLen;

			// 命令代码//保持不变
			fieldLen = 1;
			pos = pos + fieldLen;

			// 帧序号//保持不变
			fieldLen = 2;
			pos = pos + fieldLen;

			// 状态
			data[pos] = (byte) (CRCOk ? 0x0 : 0x1);// 终端认证状态符，其中：00h – 认证成功，01h
													// – 认证失败
			fieldLen = 1;
			pos = pos + fieldLen;

			// log.info("get:UplAttRecsCRC" + StringUtil.toHex(data) + "信息"+
			// CRCOk);
			return data;
		}

//		private int byteArrayToInt(byte[] b, int offset)
//		{
//			int value = 0;
//			for (int i = 0; i < 4; i++)
//			{
//				int shift = (4 - 1 - i) * 8;
//				value += (b[i + offset] & 0x000000FF) << shift;
//			}
//			return value;
//		}

		private byte[] checkDataCRC(boolean CRCOk, byte[] message)
		{
			// byte[] data = new byte[len-2];
			// byte CRCDate0 = message[0];
			// byte CRCDate1 = message[1];
			//
			// System.arraycopy(message, 2, data, 0, len-2);
			//
			// byte[] CRCresult= CodecUtil.crc16Bytes(data);
			// boolean CRCOk = CRCresult[0] == CRCDate0 && CRCresult[1] ==
			// CRCDate0;

			byte commandCode = message[3];// 得到命令代码
			switch (commandCode)
			{
			case 2:// 终端认证

//				log.info("get: 终端认证信息" + CRCOk);
				return terAuthenCRC(CRCOk, message);

			case 3:// 时间校验
//				log.info("get: 时间校验" + CRCOk);
				return TimeSyncCRC(CRCOk, message);

			case 113:// 上传到离校记录
			case 114:// 上传终端状态
			case 115:// 上传触发器状态
			case 116:// 上传标签电量报警
				return UplAttRecsCRC71To74(CRCOk, message);

			}
//			log.info("get2:命令不对" + commandCode);
			return UplAttRecsCRC71To74(CRCOk, message);
		}

		private boolean UplAttRecsParse(byte[] messgeData)
		{
			// 帧选项
			int pos = 0;// 启始从帧选项
			int fieldLen = 1;// 数据项长度
			pos = pos + fieldLen;// 1

			// 协议代码//保持不变
			fieldLen = 2;
			pos = pos + fieldLen;// 3

			// 命令代码//保持不变
			fieldLen = 1;
			pos = pos + fieldLen;// 4

			// 帧序号//保持不变
			fieldLen = 2;
			pos = pos + fieldLen;// 6

			// 终端ID号//保持不变
			fieldLen = 18;
			byte[] deviceNoBytes = new byte[fieldLen];
			System.arraycopy(messgeData, pos, deviceNoBytes, 0, fieldLen);
			String deviceNo = new String(deviceNoBytes);
			pos = pos + fieldLen;// 24

			// 考勤记录总数//保持不变
			fieldLen = 1;
			int len = messgeData[pos];
			pos = pos + fieldLen;// 25

			for (int i = 0; i < len; i++)
			{
				// 卡号
				fieldLen = 4;
				byte[] cardNoBytes = new byte[fieldLen];
				System.arraycopy(messgeData, pos, cardNoBytes, 0, fieldLen);
				// String cardNo = StringUtil.toHex(cardNoBytes);
//				String cardNo = "" + byteArrayToInt(cardNoBytes, 0);
				String cardNo = "" + toCardCode(cardNoBytes);
				pos = pos + fieldLen;

				//年
				fieldLen = 2;
				byte[] yearBytes = new byte[fieldLen];
				System.arraycopy(messgeData, pos, yearBytes, 0, fieldLen);
				String year = ""+toCardCode(yearBytes);
				pos = pos + fieldLen;
				
				//月
				fieldLen = 1;
				byte[] monthBytes = new byte[fieldLen];
				System.arraycopy(messgeData, pos, monthBytes, 0, fieldLen);
				String month = ""+toCardCode(monthBytes);
				pos = pos + fieldLen;
				
				//日
				fieldLen = 1;
				byte[] dayBytes = new byte[fieldLen];
				System.arraycopy(messgeData, pos, dayBytes, 0, fieldLen);
				String day = ""+toCardCode(dayBytes);
				pos = pos + fieldLen;
				
				//时
				fieldLen = 1;
				byte[] hourBytes = new byte[fieldLen];
				System.arraycopy(messgeData, pos, hourBytes, 0, fieldLen);
				String hour = ""+toCardCode(hourBytes);
				pos = pos + fieldLen;
				
				//分
				fieldLen = 1;
				byte[] minuteBytes = new byte[fieldLen];
				System.arraycopy(messgeData, pos, minuteBytes, 0, fieldLen);
				String minute = ""+toCardCode(minuteBytes);
				pos = pos + fieldLen;
				
				//秒
				fieldLen = 1;
				byte[] secondBytes = new byte[fieldLen];
				System.arraycopy(messgeData, pos, secondBytes, 0, fieldLen);
				String second = ""+toCardCode(secondBytes);
				pos = pos + fieldLen;
				
				String time = year + "-" + month+ "-" + day + " " + hour + ":" + minute + ":" +second;

				// 状态
				fieldLen = 1;

				int inOut = messgeData[pos];

				pos = pos + fieldLen;

//				log.info("get: deviceNo:"+deviceNo+" cardoNo:" + cardNo + " time:" + time + " inOut:" + inOut);

				trunInOut(deviceNo, cardNo, inOut, ip,time);

				// 在此处,进行进出校的处理

				// 参见trun()函数，把电池一直设置为1，inOut,1为进，2为出

			}

			return true;
		}

		private boolean UplTagAlarmParse(byte[] messgeData)
		{
			// 帧选项
			int pos = 0;// 启始从帧选项
			int fieldLen = 1;// 数据项长度
			pos = pos + fieldLen;

			// 协议代码//保持不变
			fieldLen = 2;
			pos = pos + fieldLen;

			// 命令代码//保持不变
			fieldLen = 1;
			pos = pos + fieldLen;

			// 帧序号//保持不变
			fieldLen = 2;
			pos = pos + fieldLen;

			// 终端ID号//保持不变
			fieldLen = 18;
			pos = pos + fieldLen;

			// 标签记录总数//保持不变
			fieldLen = 1;
			int len = messgeData[pos];
			pos = pos + fieldLen;

			for (int i = 0; i < len; i++)
			{
				// 卡号
				fieldLen = 4;
				byte[] cardNoBytes = new byte[fieldLen];
				System.arraycopy(messgeData, pos, cardNoBytes, 0, fieldLen);
				String cardNo = StringUtil.toHex(cardNoBytes);
				pos = pos + fieldLen;

				// 状态
				fieldLen = 1;

				int status = messgeData[pos];
				pos = pos + fieldLen;

//				log.info("get: cardoNo:" + cardNo + " status:" + status);

				// 在此处,进行进出校的处理

				// 参见trun()函数，把电池一直设置为1，inOut,1为进，2为出

				trunTagAlarm(cardNo, ip);
			}

			return true;
		}

		private boolean parseProtocol(byte[] message)
		{
			byte commandCode = message[3];// 得到命令代码
			switch (commandCode)
			{
			case 2:// 终端认证

//				log.info("getPar: 终端认证信息");
				return true;

			case 3:// 时间校验
//				log.info("getPar: 时间校验");
				return true;

			case 113:// 上传到离校记录
				return UplAttRecsParse(message);

			case 114:// 上传终端状态
				break;
			case 115:// 上传触发器状态
				break;
			case 116:// 上传标签电量报警
				return UplTagAlarmParse(message);

			}
//			log.info("get1:命令不对" + commandCode);
			return true;

		}

		private byte[] combineData(byte[] rData)
		{
			int len = 8;
			byte[] combineData = new byte[rData.length + len];
			System.arraycopy(rData, 0, combineData, len, rData.length);

			// 前导码
			combineData[0] = (byte) 0xff;
			combineData[1] = (byte) 0xff;
			combineData[2] = (byte) 0xff;
			combineData[3] = (byte) 0xff;

			// 数据长度
			byte[] lenByte = CodecUtil.short2bytes((short) (rData.length + 2));
			combineData[4] = lenByte[0];
			combineData[5] = lenByte[1];

			// 验证校验和
			byte[] CRCresult = CodecUtil.crc16Bytes(rData);
			combineData[6] = CRCresult[0];
			combineData[7] = CRCresult[1];

			return combineData;
		}

		private int getDataLen(byte[] lenField)
		{
			int len = 0;
			len = 256 * byteToInt(lenField[0]) + byteToInt(lenField[1]);
			return len;

		}

		public void run()
		{
//			log.info("SocketHandle[" + this.getName() + "]开始：" + ip);

			try
			{
				in = socket.getInputStream();
				out = socket.getOutputStream();
			} catch (IOException e)
			{
				e.printStackTrace();
			}

			if (in != null)
			{
				byte beginChar = 0; // 读前导符，四个F
				int pos = 0; // 记前导符个数
				int len = 0; // 记数据内容长度
				// boolean isOK = false;//记是否接收成功
				String sss = "";// 临时显示字符串
				for (;;)
				{
					if (socket.isClosed())
						break;
					try
					{
						beginChar = (byte) in.read();
						if (beginChar != -1)
						{
							pos = 0;
							try
							{
								sleep(100);
							} catch (InterruptedException e)
							{
								break;
							}
							continue;
						} else
						{

							// log.info("get:" + pos +":"+ beginChar + "信息");
							pos++;
							if (pos < 4) // 四个前导码
							{
								continue;
							}

						}

						// 处理数据，先处理长度
						pos = 0;
						byte[] lenField = new byte[2];
						in.read(lenField);
						len = getDataLen(lenField) - 2;// 消息数据长度，去掉两位的校验
						// 读取2个字节校验
						byte CRCData0 = (byte) in.read();
						byte CRCData1 = (byte) in.read();

						if(len > 0){
							// 读取数据
							byte[] message = new byte[len];
							in.read(message);
							sss = StringUtil.toHex(message);
//							log.info("get:" + sss + "信息");

							// 验证校验和
							byte[] CRCresult = CodecUtil.crc16Bytes(message);
							boolean CRCOk = CRCresult[0] == CRCData0 && CRCresult[1] == CRCData1;
							sss = StringUtil.toHex(CRCresult);
//							log.info("get:" + sss + "信息" + CRCOk);

							// 得到检测后将返回给客户端的数据
							byte[] rData = checkDataCRC(CRCOk, message);
							// 将数据组合，并发给客户端数据

							// log.info("get:" + StringUtil.toHex(rData) +
							// "信息write");
							// log.info("get:" +
							// StringUtil.toHex(combineData(rData)) + "信息write2");
							out.write(combineData(rData));
							// if (CRCOk)
							{
								parseProtocol(message);
							}
						}else{
							//TODO 目前显示手动给响应  
							byte[] message = new byte[]{-64, -32, 2, 114, 2, 127, 83, 85, 80, 69, 82, 46, 51, 53, 48, 57, 48, 50, 48, 48, 50, 55, 48, 52, 2, 1, 0, 1, 0};
							byte[] rData = checkDataCRC(true, message);
							out.write(combineData(rData));
							log.info("getDataLen(lenField) - 2为负数");
						}
					} catch (IOException e)
					{
						break;
					}
				}
			}

//			log.info("SocketHandle[" + this.getName() + "]结束：" + ip);
			sockets.remove(this);
		}

		public void close()
		{
			try
			{
				in.close();
			} catch (IOException e1)
			{
				e1.printStackTrace();
			}
			try
			{
				socket.close();
				socket = null;
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			this.interrupt();
		}
	}

	public Long toCardCode(byte[] cardNoBytes)
	{
		String result = "";
		for (int i = 0; i < cardNoBytes.length; i++)
		{
			result += Integer.toString((cardNoBytes[i] & 0xff) + 0x100, 16).substring(1);
		}
		return Long.parseLong(result, 16);
	}

}
