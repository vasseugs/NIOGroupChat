# NIOGroupChat

This program represents basic NIO client-server model. It consists of 3 parts -
the server, the client and the chat window. I added a separate chat window because I find this the 
most convinient way to display chat messages. In fact, the chat window is like a client, but the only thing
it does is reading and displaying incoming messages from users in this chat.

It's not supposed to use this program to commuticate, it's just me practicing in NIO. I'm not familiar with
GUI frameworks yet, so the code runs only in console.

# How to use

In this program, you have the ProgramConstants class. You can specify you server address parameters there. 
First, you run RunServer program, then you run RunChatWindow and, at last, you run RunClient. Follow the
instructions in RunClient. All messages will be displayed in RunChatWindow. RunServer program doesn't
make any notifications. You can run as many clients and chat windows as you want. In fact, a particular chat window does
not matched with particular client.

If you want to run this program on one machine, don't change anything. 

If you want to run this program on different machines, make sure that you adjusted address parameters properly.
I tested it over wifi on Windows 10. Firstly, I entered "ipconfig" in command line and copied the IP address of the
computer which server will be going to run on and pasted it into the ProgramConstants.SERVER_HOST. Then I specified the same 
IP address ProgramConstants.SERVER_HOST on another computer. The, the port value must be the same on all computers running
this program. 

