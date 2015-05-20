package com.thiagowittmann.attend;

/**
 * Created by thiago on 5/20/15.
 */
public class Talk {
    private String id;
    private String name;
    private String speaker;

    public Talk(String id, String name, String speaker){
        this.id = id;
        this.name = name;
        this.speaker = speaker;
        System.out.println(this.id);
    }

    public String getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }

    public String getSpeaker(){
        return this.speaker;
    }

    @Override
    public String toString() {
        return this.getSpeaker() + ": " + this.getName();
    }
}
