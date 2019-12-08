# StarWar-Messenger
StarWar Messenger Application

Server

The server has the following responsibilities -

User Authentication - When a new client requests for a connection, the server should prompt the user to input the username and password and authenticate the user. The valid username and password combinations will be stored in a file called credentials.txt which will be in the same directory as the server program. An example credentials.txt file is provided on the assignment page. Username and passwords are case-sensitive. We may use a different file for testing so DO NOT hardcode this information in your program. You may assume that each username and password will be on a separate line and that there will be one white space between the two. If the credentials are correct, the client is considered to be logged in (i.e. online) and a welcome message is displayed. When all messaging is done, a user should be able to logout from the server.
On entering invalid credentials, the user is prompted to retry. After 3 consecutive failed attempts, the user is blocked for a duration of block_duration seconds (block_duration is a command line argument supplied to the server) and cannot login during this duration (even from another IP address). While a user is online, if someone uses the same username/password to log in (even from another IP address), then this new login attempt is denied.
Timeout - The server should keep track of all online users. If the server does not receive any commands from a user for a period of timeout seconds (timeout is a command line argument supplied to the server), then the server should automatically log this user out. Note that, to be considered active, a user must actively issue a command. The receipt of a message does not count.
Presence Broadcasts - The server should notify the presence/absence of other users logged into the server, i.e. send a broadcast notification to all online users when a user logs in and logs out.
List of online users - The server should provide a list of users that are currently online in response to such a query from a user.
Online history – The sever should provide a list of users that logged in for a user specified time in the past (e.g. users who logged in within the past 15 minutes).
Message Forwarding - The server should forward each instant message to the correct recipient assuming they are online.
Offline Messaging - When the recipient of a message is not logged in (i.e. is offline), the message will be saved by the server. When the recipient logs in next, the server will send all the unread messages stored for that user (timestamps are not required).
Message Broadcast – The server should allow a user to broadcast a message to all online users. Offline messaging is not required for broadcast messages.
Blacklisting - The server should allow a user to block / unblock any other user. For example, if user A has blocked user B, B can no longer send messages to A i.e. the server should intercept such messages and inform B that the message cannot be forwarded. Blocked users also do not get presence notifications i.e. B will not be informed each time A logs in or logs out.  


Client

The client has the following responsibilities -

Authentication - The client should provide a login prompt to enable the user to authenticate with theserver.  
Message - The client should allow the user to send a message to any other user and display messages sent by other users. The client should also allow the user to send a broadcast message to all online users.
Notifications - The client should display presence notifications sent by the server about users logging in and out from the server.
Find users online - The client should provide a way for the user to obtain a list of all the users currently online from the server.
Find online history – The client should provide a way for the user to obtain a list of all users who had logged in within a user specified time period.
Blacklist – The client should allow a user to block a user from sending any further messages. The client should also allow a user to unblock a user that was earlier blocked.

Commands supported by the client
After a user is logged in, the client should support all the commands shown in the table below. For the following, assume that commands were run by user A.
Command: Description
message <user> <message> 
Send <message> to <user> through the server. If the user is online then deliver the message immediately, else store the message for offline delivery. If <user> has blocked A, then a message to that effect should be displayed for A. If the <user> is not present in the credentials file (i.e. invalid user) or is self (A) then an appropriate error message should be displayed. The <message> used in our tests will be a few words at most.

broadcast <message> 
Send <message> to all online users except A and those users who have blocked A. Inform A that message could not be sent to some recipients.

whoelse 
This should display the names of all users that are currently online excluding A. Users can be displayed in any order.

whoelsesince <time> 
This should display the names of all users who were logged in at any time within the past <time> seconds excluding A. 
Note that this, may include users that may currently be offline. If <time> is greater than the time since when the server has been running, then all users who logged in since the sever initiation time should be listed. This suggests that you will need to keep a login history since the start of the server. Users can be displayed in any order.

block <user>
blocks the <user> from sending messages to A. A message should be displayed to A confirming the blocking action. If <user> is self (i.e., A) or is invalid, then an appropriate error message should be displayed. <user> must not be informed that A has blocked them.

unblock <user>
unblocks the <user> who has been previously blocked by A. A message should be displayed to A confirming the unblocking action. If <user> is self (i.e., A) or is invalid or was not already blocked, then an appropriate error message should be displayed.
logout log out user A.
Any command that is not listed above should result in an error message being displayed to the user. The interaction with the user should be via the terminal (i.e. console). All messages must be displayed in the same terminal. There is no need to create separate terminals for messaging with different users.
We do not mandate the exact text that should be displayed by the client to the user for the various messages. However, you must make sure that the displayed text is easy to comprehend. Please make sure that you DO NOT print any debugging information on the client terminal.
We also prefer that you do not print anything at the terminal running the server. We suggest that you use an optional debug flag (e.g. –d) for the server. When this flag is turned on, your server can print debugging information to the terminal.
