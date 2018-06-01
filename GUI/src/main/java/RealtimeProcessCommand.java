class RealtimeProcessCommand{  
    private String directory = null;  
    private String command = null;  
    public RealtimeProcessCommand(){}  
      
    public void setDirectory(String directory){  
        this.directory = directory;  
    }  
    public void setCommand(String command){  
        this.command = command;  
    }  
    public String getDirectory(){  
        return this.directory;  
    }  
    public String getCommand(){  
        return this.command;  
    }  
      
}