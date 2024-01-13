package cctvcat.saction;

interface ISaService {

    void exit();

    boolean isAlive();

    String getVersion();

    IBinder getRegisteredBinder(String key);

}