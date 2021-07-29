package com.fuchentao.seckill.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fuchentao.seckill.domain.SeckillUser;


public class SeckillUserUtil {
	
	private static void createUser(int count) throws Exception{
		List<SeckillUser> users = new ArrayList<SeckillUser>(count);
		//生成用户
		for(int i=0;i<count;i++) {
			SeckillUser seckillUser = new SeckillUser();
			seckillUser.setId(100L + i);
			seckillUser.setLoginCount(1);
			seckillUser.setNickname(String.valueOf(13000000000L + i));
			seckillUser.setRegisterDate(new Date());
			seckillUser.setSalt("1a2b3c");
			seckillUser.setPassword( MD5Util.inputPasswordToDBPassword
                                         ("123456", seckillUser.getSalt()) );
			users.add(seckillUser);
		}
		System.out.println("create user");

		//插入数据库
		Connection connection = DBUtil.getConn();
		String sql =
				"insert into seckill_user" +
                "(id, nickname, password, salt, register_date, login_count) " +
                "values(?,?,?,?,?,?)";
		PreparedStatement pstmt = connection.prepareStatement(sql);
		for(int i = 0; i < users.size(); i++) {
			SeckillUser seckillUser = users.get(i);
			pstmt.setInt(6, seckillUser.getLoginCount());
			pstmt.setString(2, seckillUser.getNickname());
			pstmt.setTimestamp(5,
                                new Timestamp(seckillUser.getRegisterDate().getTime()));
			pstmt.setString(4, seckillUser.getSalt());
			pstmt.setString(3, seckillUser.getPassword());
			pstmt.setLong(1, seckillUser.getId());
			pstmt.addBatch();
		}
		pstmt.executeBatch();
		pstmt.close();
		connection.close();
		System.out.println("insert to db");

		//登录，生成token
        String urlString = "http://localhost:8080/login/do_login";
//		String urlString = "http://localhost:8080/login/create_token";
		File file = new File("D:/tokens.txt");
		if(file.exists()) {
			file.delete();
		}
		RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
		file.createNewFile();
		randomAccessFile.seek(0);
		for(int i = 0; i < users.size(); i++) {
            SeckillUser user = users.get(i);
			URL url = new URL(urlString);
			HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setDoOutput(true);
			OutputStream out = httpURLConnection.getOutputStream();
			String params = "mobile=" + user.getNickname() +
                            "&password=" + MD5Util.inputPasswordToFormPassword("123456");
			out.write(params.getBytes());
			out.flush();
			InputStream inputStream = httpURLConnection.getInputStream();
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			byte buff[] = new byte[1024];
			int len = 0;
			while((len = inputStream.read(buff)) >= 0) {
				bout.write(buff, 0 ,len);
			}
			inputStream.close();
			bout.close();
			String response = new String(bout.toByteArray());
//			JSONObject jo = JSON.parseObject(response);
//			String token = jo.getString("data");
			System.out.println("create token : " + user.getId());
			
			String row = user.getNickname() + "," + response;
			randomAccessFile.seek(randomAccessFile.length());
			randomAccessFile.write(row.getBytes());
			randomAccessFile.write("\r\n".getBytes());
			System.out.println("write to file : " + user.getId());
		}
		randomAccessFile.close();
		
		System.out.println("over");
	}
	
	public static void main(String[] args)throws Exception {
		createUser(3);
	}
}
