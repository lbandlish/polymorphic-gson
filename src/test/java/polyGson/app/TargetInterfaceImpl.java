package polyGson.app;

public class TargetInterfaceImpl implements TargetInterface {

    public String message;

    public TargetInterfaceImpl(String message) {
        this.message = message;
    }

    @Override
    public void callMe() {
        System.out.println("targetInterfaceImpl_callMe");
    }
}
