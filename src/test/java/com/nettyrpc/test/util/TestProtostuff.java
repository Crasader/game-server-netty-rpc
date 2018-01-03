package test.java.com.nettyrpc.test.util;

import java.io.ByteArrayOutputStream;

import org.junit.Assert;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

import junit.framework.TestCase;
import test.java.com.nettyrpc.test.util.entity.A6;

/**
 * 测试protostuff
 * @author tonho
 *
 */
public class TestProtostuff extends TestCase{
	/** 
	 * Simple serialization/deserialization test
	 * @throws Exception
	 */
	public void testSerializeDeserialize() throws Exception {
	  RuntimeSchema<A6> schema=RuntimeSchema.createFrom(A6.class);
	  A6 source=new A6(42);
	  ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
	  LinkedBuffer buffer=LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
	  ProtostuffIOUtil.writeTo(outputStream,source,schema,buffer);
	  byte[] bytes=outputStream.toByteArray();
	  A6 newMessage=schema.newMessage();
	  ProtostuffIOUtil.mergeFrom(bytes,newMessage,schema);
	  Assert.assertEquals(source,newMessage);
	}
	

}
