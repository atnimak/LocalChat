package com.localchat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static class Handler extends Thread{
        private Socket socket;
        public Handler(Socket socket){
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message answer = connection.receive();

                if (answer.getType() == MessageType.USER_NAME) {

                    if (!answer.getData().isEmpty()) {
                        if (!connectionMap.containsKey(answer.getData())) {
                            connectionMap.put(answer.getData(), connection);
                            connection.send(new Message(MessageType.NAME_ACCEPTED));
                            return answer.getData();
                        }
                    }
                }
            }
        }

       private void sendListOfUsers(Connection connection, String userName) throws IOException{
            for(Map.Entry entry:connectionMap.entrySet()){
                String name = (String) entry.getKey();
                if(name!=userName){
                    connection.send(new Message(MessageType.USER_ADDED,name));
                }
            }

        }

      private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{
            while(true){
                Message message = connection.receive();
                if(message!=null && message.getType()==MessageType.TEXT){
                    sendBroadcastMessage(new Message(MessageType.TEXT,userName+": "+message.getData()));
                }else {
                    ConsoleHelper.writeMessage("Сообщение не является текстом! Ошибка!");
                }
            }
       }

        public void run(){
            ConsoleHelper.writeMessage("Установлено соединение с"+socket.getRemoteSocketAddress());
            String username=null;
            try {
                Connection connection = new Connection(socket);
                username = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED,username));
                sendListOfUsers(connection,username);
                serverMainLoop(connection,username);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом");
            } catch (ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом");
            }finally {
                if(username!=null){
                    connectionMap.remove(username);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED,username));
                }

            }
        }
    }

    private static Map<String, Connection> connectionMap=new ConcurrentHashMap<>();

    static void sendBroadcastMessage(Message message){
        for(Connection connection:connectionMap.values()){
            try {
                connection.send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("Сообщение не отправлено");
            }


        }
    }

    public static  void main(String[] args) {
        ConsoleHelper.writeMessage("Введите порт сервера");


       try( ServerSocket serverSocket = new ServerSocket(ConsoleHelper.readInt())){
           ConsoleHelper.writeMessage("Сервер запущен");

           while(true){
               Socket socket = serverSocket.accept();
               Handler handler = new Handler(socket);
               handler.start();
           }
       }catch (IOException e){
           ConsoleHelper.writeMessage("Ошибка. Попробуйте еще раз");
       }


    }
}
