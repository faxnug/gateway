package thirdpart.uuid;

public class WhyUuid {
    //将构造方法定义成私有静态类，这样可以直接调用，不用重新定义构造函数
    private static WhyUuid ourInstance = new WhyUuid();

    public static WhyUuid getInstance(){
        return ourInstance;
    }

    private WhyUuid(){}

    private SnowflakeIdWorker idWorker;

    /**
     * 初始化
     * @param centerId
     * @param workerId
     */
    public void init(long centerId, long workerId){
        idWorker = new SnowflakeIdWorker(centerId, workerId);
    }

    /**
     * 生成 UUID
     * @return
     */
    public long getUUID(){
        return idWorker.nextId();
    }

}
