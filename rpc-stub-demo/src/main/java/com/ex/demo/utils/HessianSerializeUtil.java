package com.ex.demo.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;

import cn.hutool.core.io.IoUtil;

@SuppressWarnings("unchecked")
public class HessianSerializeUtil {

	public static <T> byte[] serialize(T obj) {
		ByteArrayOutputStream byteArrayOutputStream = null;
		HessianOutput hessianOutput = null;
		try {
			byteArrayOutputStream = new ByteArrayOutputStream();
			// Hessian的序列化输出
			hessianOutput = new HessianOutput(byteArrayOutputStream);
			hessianOutput.writeObject(obj);
			return byteArrayOutputStream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IoUtil.closeIfPosible(byteArrayOutputStream);
			IoUtil.closeIfPosible(hessianOutput);
		}
		return null;
	}

	public static <T> T deserialize(byte[] data, Class<T> cls) {
		ByteArrayInputStream byteArrayInputStream = null;
		HessianInput hessianInput = null;
		try {
			byteArrayInputStream = new ByteArrayInputStream(data);
			// Hessian的反序列化读取对象
			hessianInput = new HessianInput(byteArrayInputStream);
			return (T) hessianInput.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IoUtil.closeIfPosible(byteArrayInputStream);
			IoUtil.closeIfPosible(hessianInput);
		}
		return null;
	}
}
