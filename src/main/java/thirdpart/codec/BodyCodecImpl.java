package thirdpart.codec;

import com.alipay.remoting.serialization.SerializerManager;

import java.io.Serializable;

public class BodyCodecImpl implements IBodyCodec {
    @Override
    public <T> byte[] serialize(T obj) throws Exception {
        //1.jdk序列化//2.json//3.自定义算法（Hessian2）
        return SerializerManager.getSerializer(SerializerManager.Hessian2).serialize(obj);
        //return new byte[0];
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws Exception {
        return SerializerManager.getSerializer(SerializerManager.Hessian2).deserialize(bytes, clazz.getName());
    }

    static class A implements Serializable {
        private String a;
    }

    public static void main(String[] args) throws Exception {
        A a = new A();
        a.a = "test";

        byte[] serialize = new BodyCodecImpl().serialize(a);
        A deserialize = new BodyCodecImpl().deserialize(serialize, A.class);
        System.out.println(deserialize.a);
    }
}
