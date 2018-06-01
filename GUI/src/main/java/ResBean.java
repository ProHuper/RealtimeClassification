public class ResBean {

    private String info;
    private String type;
    private boolean saved;

    ResBean(String info, String type, boolean saved){
        this.info = info;
        this.type = type;
        this.saved = saved;
    }

    public boolean isSaved(){
        return saved;
    }

    public String getInfo(){
        return info;
    }

    public String getType(){
        return type;
    }
}
