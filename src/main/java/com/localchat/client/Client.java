package com.localchat.client;

import com.localchat.Connection;
import com.localchat.ConsoleHelper;
import com.localchat.Message;
import com.localchat.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;

    public class SocketThread extends Thread{
       protected void processIncomingMessage(String message){
           ConsoleHelper.writeMessage(message);
       }

        protected void informAboutAddingNewUser(String userName){
           ConsoleHelper.writeMessage(userName+" присоединился к чату");
        }

        protected void informAboutDeletingNewUser(String userName){
            ConsoleHelper.writeMessage(userName+" покинут чат");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected){
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this){
                Client.this.notify();
            }
        }

        protected  void clientHandshake() throws IOException, ClassNotFoundException{
            while(true){
                Message message = connection.receive();
                if(message.getType()==MessageType.NAME_REQUEST){
                    connection.send(new Message(MessageType.USER_NAME,getUserName()));
                }else if(message.getType()==MessageType.NAME_ACCEPTED){
                    notifyConnectionStatusChanged(true);
                    return;
                }else {
                    throw new IOException("Unexpected MessageType");
                }
            }


        }

       protected void clientMainLoop() throws IOException, ClassNotFoundException{
            while(true){
                Message message = connection.receive();
                if(message.getType()==MessageType.TEXT){
                    processIncomingMessage(message.getData());
                }else if(message.getType()==MessageType.USER_ADDED){
                    informAboutAddingNewUser(message.getData());
                }else if(message.getType()==MessageType.USER_REMOVED){
                    informAboutDeletingNewUser(message.getData());
                }else {
                    throw new IOException("Unexpected MessageType");
                }
            }

       }

       public void run(){
           try {
               connection=new Connection( new Socket(getServerAddress(),getServerPort()));
               clientHandshake();
               clientMainLoop();

           } catch (IOException e) {
               notifyConnectionStatusChanged(false);
            } catch (ClassNotFoundException e) {
               notifyConnectionStatusChanged(false);
           }
       }







    }

    protected String getServerAddress(){
        ConsoleHelper.writeMessage("Введите IP адрес сервера. Если сервер запущен на этой же машине введите localhost:");
        return ConsoleHelper.readString();
    }

    protected int getServerPort(){
        ConsoleHelper.writeMessage("Введите адрес порта сервера:");
        return ConsoleHelper.readInt();
    }

    protected String getUserName(){
        ConsoleHelper.writeMessage("Введите имя пользователя:");
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole(){
        return true;
    }

    protected SocketThread getSocketThread(){
        return new SocketThread();
    }

    protected void sendTextMessage(String text){

        try {
            Message message = new Message(MessageType.TEXT,text);
            connection.send(message);
        } catch (IOException e) {
            clientConnected=false;
           ConsoleHelper.writeMessage("Сообщение не было отправлено! Ошибка соединения.");

        }
    }

    public void run(){
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        synchronized (this){
            try {
                wait();
            } catch (InterruptedException e) {
                ConsoleHelper.writeMessage("Ошибка потока!");
                System.exit(1);
            }
        }

        if(clientConnected){
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду ‘exit’.");
            while(clientConnected){
                String exit = ConsoleHelper.readString();
                if(exit.equals("exit")) clientConnected=false;
                if(shouldSendTextFromConsole()){
                    sendTextMessage(exit);
                }
            }

        }else{
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        }

    }

    public static void main(String... args){
        Client client = new Client();
        client.run();
    }

}
