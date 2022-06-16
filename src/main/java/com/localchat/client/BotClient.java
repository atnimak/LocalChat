package com.localchat.client;

import com.localchat.ConsoleHelper;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BotClient extends Client {

    @Override
    protected SocketThread getSocketThread(){
        return new BotSocketThread();
    }
    @Override
    protected boolean shouldSendTextFromConsole(){
        return false;
    }

    @Override
    protected String getUserName(){
        return "date_bot_"+((int)(Math.random() * 100));
    }

    public static void main(String... args){
        new BotClient().run();
    }

    public class BotSocketThread extends SocketThread{

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();

        }

        @Override
        protected void processIncomingMessage(String message){
            ConsoleHelper.writeMessage(message);

            String username="";
            String data="";


            if(message.contains(": ")){
                String[] usernameAndData = message.split(": ");
                username = usernameAndData[0];
                data = usernameAndData[1];
            }else {
                data = message;
            }


            SimpleDateFormat dateFormat = null;
            if(data.equals("дата")){
                dateFormat=new SimpleDateFormat("d.MM.YYYY");

            }else if(data.equals("день")){
                dateFormat=new SimpleDateFormat("d");

            }else if(data.equals("месяц")){
                dateFormat=new SimpleDateFormat("MMMM");

            }else if(data.equals("год")){
                dateFormat=new SimpleDateFormat("YYYY");

            }else if(data.equals("время")){
                dateFormat=new SimpleDateFormat("H:mm:ss");

            }else if(data.equals("час")){
                dateFormat=new SimpleDateFormat("H");

            }else if(data.equals("минуты")){
                dateFormat=new SimpleDateFormat("m");

            }else if(data.equals("секунды")){
                dateFormat=new SimpleDateFormat("s");

            }
            if(dateFormat != null){
               sendTextMessage("Информация для "+username+": "+dateFormat.format(Calendar.getInstance().getTime()));
            }


        }


    }
}
