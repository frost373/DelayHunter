package top.thinkin.delayhunter.error;


public class NodesException extends RuntimeException {

    private final String redirect;


    public NodesException( String errmsg,String redirect) {
        super(errmsg);
        this.redirect = redirect;
    }



    public String getErrmsg() {
        return super.getMessage();
    }



    public String getRedirect() {
        return redirect;
    }
}
