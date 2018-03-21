// Client.java
// $Id: Client.java,v 1.1 2010/06/15 12:25:44 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2001.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http.cache.push;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.net.Socket;
import java.net.InetAddress;
import java.net.ConnectException;
import java.net.UnknownHostException;

/**
 * PushCache Client - send messages to push cache server 
 *
 * @author Paul Henshaw, The Fantastic Corporation, Paul.Henshaw@fantastic.com
 * @version $Revision: 1.1 $
 * $Id: Client.java,v 1.1 2010/06/15 12:25:44 smhuang Exp $
 */
public class Client {
    /** 
     * Return/exit values
     */
    public static final int       OK=0, NO=1, FAILED=-1;

    /**
     * How many times should we try to reconnect?  10
     */
    public static final int       MAX_TRIES=10;
    
    /**
     * Default hostname - localhost 
     */
    public static final String    DEFAULT_SERVER="localhost";

    /**
     * Version Control Revision
     */
    public static final String    VC_REVISION="$Revision: 1.1 $";

    /**
     * Version Control Id 
     */
    public static final String    VC_ID=
	"$Id: Client.java,v 1.1 2010/06/15 12:25:44 smhuang Exp $";

    private int                   _verbose=0;
    private int                   _port=-1;
    private String                _host_name="";
    private byte[]                _buffer=null;
    private byte[]                _text_buffer=null;
    private Socket                _socket=null;
    private InputStream           _in=null;
    private OutputStream          _out=null;
    private DataInputStream       _data_stream=null;
    private ByteArrayOutputStream _baos=null;
    private DataOutputStream      _dos=null;
    private int                   _reply_com=-1;
    private String                _reply_command;

    /**
     * Construct a Client for the default hostname and port number 
     */
    public Client()  {
	_host_name=DEFAULT_SERVER;
	_port=PushCacheFilter.DEFAULT_PORT_NUM;
    }

    /**
     * Construct a Client for the specified hostname and port number
     */
    public Client(String hostname, int port) {
	_host_name=hostname;
	_port=port;
    }

    /**
     * Construct client with command line arguments and execute
     * command.  Called by main, for use as stand alone program - 
     * calls System.exit at the end of execution 
     */
    public Client(String[] argv) throws IOException {
	handleArgs(argv);
    }

    /**
     * Display version information
     */
    protected void version() {
	System.err.println("Jigsaw Push Cache Java Client 1.0 "+
			   "by Paul Henshaw, The Fantastic Corporation\n"+
			   VC_REVISION+"\n"+VC_ID+"\n");
    }

    /**
     * Display usage/help message
     */
    protected void usage() {
	System.err.println("Usage: -c <command> [-u <url>] [ -p <path>] "+
	   "[ -s <server>] [ -P <port> ] "+
	   "[ -v ] [ -h ] [ -V ]\n"+
	   "\t-s\t\t connect to specified server, default is "+
	   DEFAULT_SERVER+".\n"+
	   "\t-P\t\t connect to specified port, default is "+
	   PushCacheFilter.DEFAULT_PORT_NUM+".\n"+
	   "\t-v\t\t verbose operation, repeat for more verbosity.\n"+
	   "\t-h\t\t show this help message.\n"+
	   "\t-V\t\t show version information.\n\n"+
	   "Commands recognised:\n"+
	   "\tADD\t\t add <path> to cache as if downloaded from <url>.\n"+
	   "\tDEL\t\t delete <url> from cache.\n"+
	   "\tPRS\t\t check if <url> is present in cache.\n"+
	   "\tCLN\t\t clean cache.\n"+
	   "\tNOP\t\t do nothing.\n\n"+
	   "Notes:\n"+
	   "\tURLs should be fully qualified including trailing slashes\n"+
	   "\tfor directories, e.g.:\n"+
	   "\t\thttp://www.fantastic.com/\n\trather than:"+
	   "\n\t\thttp://www.fantastic.com\n\n"+
	   "\tBy default the operation of the PRS command is silent, \n"+
	   "\tuse the -v flag to display a text message indicating the\n"+
	   "\tpresence of a URL in the cache.\n\n"+
	   "\tWhen ADDing a file only the <path> is sent to the server\n"+
	   "\tNOT the contents of the file, this means that files can\n"+
	   "\tbe added to remote caches only if the client and server\n"+
	   "\tshare a file system via NFS or similar.\n\n"+
	   "Exit Value:\n"+
	   "\tThis program exits with one of the following values:\n"+
	   "\t0\t OK, command successful (PRS: URL present)\n"+
	   "\t1\t (PRS only) command successful, URL not present\n"+
	   "\t255\t command unsuccessful\n");
    }

    /**
     * Interpret command line arguments
     */
    protected void handleArgs(String[] argv) throws IOException {
	String url=null,path=null,com=null;
	char c;
	boolean got_com=false;
	boolean error=false;

	for(int i=0; i<argv.length; i++) {
	    if(argv[i].charAt(0)!='-' || argv[i].length()!=2) {
		error=true;
		break;
	    }
	    c=argv[i].charAt(1);
	    switch(c) {
	    case 'c':
		if(i==argv.length-1) {
		    System.err.println("Missing argument for -"+c);
		    error=true;
		    break;
		}
		com=argv[++i].toUpperCase()+"\0";
		got_com=true;
		break;

	    case 'u':
		if(i==argv.length-1) {
		    System.err.println("Missing argument for -"+c);
		    error=true;
		    break;
		}
		url=argv[++i];
		break;
		
	    case 'p':
		if(i==argv.length-1) {
		    System.err.println("Missing argument for -"+c);
		    error=true;
		    break;
		}
		path=argv[++i];
		break;
		
	    case 's':
		if(i==argv.length-1) {
		    System.err.println("Missing argument for -"+c);
		    error=true;
		    break;
		}
		_host_name=argv[++i];
		break;
		
	    case 'P':
		if(i==argv.length-1) {
		    System.err.println("Missing argument for -"+c);
		    error=true;
		    break;
		}
		try {
		    _port=Integer.parseInt(argv[++i]);
		}
		catch(Exception e) {
		    System.err.println("Invalid port number \""+argv[i]+"\"");
		    _port=-1;
		    error=true;
		}
		break;
		
	    case 'v':
		++_verbose;
		break;
		
	    case 'h':
		usage();
		System.exit(OK);
		
	    case 'V':
		version();
		System.exit(OK);
		
	    default:
		error=true;
	    }
	}

	if(!got_com) {
	    System.err.println("No command specified");
	    error=true;
	}

	if(error) {
	    usage();
	    System.exit(FAILED);
	}

	if(_port==-1) {
	    _port=PushCacheFilter.DEFAULT_PORT_NUM;
	}

	if(_host_name.length()==0) {
	    _host_name=DEFAULT_SERVER;
	}

	int ev=0;
	try {
	    switch(PushCacheProtocol.instance().parseCommand(com)) {
	    case PushCacheProtocol.ADD:
		add(path,url);
		break;
	    case PushCacheProtocol.DEL:
		del(url);
		break;
	    case PushCacheProtocol.PRS:
		if(!isPresent(url)) {
		    ev=1;
		}
		break;
	    default:
		simpleCommand(com);
	    }
	}
	catch(IllegalArgumentException e) {
	    System.err.println(e.getMessage());
	    usage();
	    ev=FAILED;
	}
	sendBye();
	System.exit(ev);
    }

    /**
     * Connect to server - try up to MAX_TRIES times, then give up
     */
    protected void connect() throws UnknownHostException, IOException {
	int tries=0;
	while(_socket==null) {
	    try {
		_socket=new Socket(_host_name,_port);
		_in=_socket.getInputStream();
		_out=_socket.getOutputStream();
	    }
	    catch(ConnectException e) {
		System.err.println("Failed to connect to "+_host_name+
				   " on port "+_port+": "+e.getMessage());
		if(++tries>MAX_TRIES) {
		    throw new 
			ConnectException("Failed to connect to "+_host_name+
					 " on port "+_port+" after "+	
					 MAX_TRIES+" attempts:\n"+e);
		}

		System.err.println("Retrying....");
		try {
		    Thread.sleep(2000);
		}
		catch(InterruptedException ex) {
		    // IGNORE
		}
	    }
	}
    }

    /**
     * Write header information into packet buffer
     */
    protected void writeHeader() throws IOException {
	_baos=new ByteArrayOutputStream();
	_dos=new DataOutputStream(_baos);
	_dos.write(PushCacheProtocol.instance().header(),0,
		   PushCacheProtocol.instance().header().length);
    }

    /**
     * Read remaining payload length bytes into buffer
     */
    protected void readPayload() throws IOException {
	_data_stream.skipBytes(PushCacheProtocol.COMMAND_LEN);
	int rlen=_data_stream.readInt();
	
	if(rlen==0) {
	    return;
	}
	_text_buffer=new byte[rlen];

	int sofar=0;
	int toread=rlen;

	sofar=0;
	toread=rlen;
	int rv=-1;
	while(toread>0) {
	    rv=_in.read(_text_buffer,sofar,toread);
	    if(rv<0) {
		throw new IOException("read returned "+rv);
	    }
	    sofar+=rv;
	    toread-=rv;
	}
	_data_stream=new DataInputStream(new 
	    ByteArrayInputStream(_text_buffer));
    }

    /**
     * Await reply packet
     */
    protected void readReply() throws IOException {
	// Read packet
	_buffer=new byte[PushCacheProtocol.PACKET_LEN];
	int rv=_in.read(_buffer);
	if(rv<0) {
	    throw new IOException("read returned "+rv);
	}
	if(rv<PushCacheProtocol.PACKET_LEN) {
	    throw new IOException("read returned less than PACKET_LEN bytes "+
				  rv);
	}

	// Check protocol tag
	if(!PushCacheProtocol.instance().isValidProtocolTag(_buffer)) {
	    throw new IOException("Bad protocol tag");
	}

	// Use a DataInputStream to read bytes and shorts
	_data_stream=
	    new DataInputStream(new ByteArrayInputStream(_buffer));
	_data_stream.skipBytes(PushCacheProtocol.TAG_LEN);

	// Check protol version
	short maj=_data_stream.readShort();
	short min=_data_stream.readShort();
	if(maj!=PushCacheProtocol.MAJ_PROTO_VERSION || 
	   min>PushCacheProtocol.MIN_PROTO_VERSION) {
	    throw new IOException("Bad protocol version");
	}

	//
	// Check command
	// Note that check includes NULL characters they are
	// part of the command.  
	//
	_reply_command=new String(_buffer, PushCacheProtocol.HEADER_LEN, 
				  PushCacheProtocol.COMMAND_LEN);
	_reply_com=PushCacheProtocol.instance().parseCommand(_reply_command);
	readPayload();
    }

    /**
     * Handle an ERR message from server - cleanup, throw IOException
     */
    protected void serverError() throws IOException {
	cleanup();
	throw new IOException("Recieved error message from server:\n"
			      +new String(_text_buffer));
    }

    /**
     * Handle an unexpected message from server - send BYE, throw IOException
     */
    protected void unexpectedReply() throws IOException {
	sendBye();
	throw new IOException("Unexpected reply from server "+
			      _reply_command);
    }

    /**
     * Cleanup, shutdown and close socket and streams
     */
    protected void cleanup() {
	try {
	    if(_in!=null) {
		_in.close();
		_in=null;
	    }
	    if(_out!=null) {
		_out.close();
		_out=null;
	    }
	    if(_socket!=null) {
//		_socket.shutdownInput();
//		_socket.shutdownOutput();
		_socket.close();
		_socket=null;
	    }
	}
	catch(java.io.IOException e) {
	    // IGNORE
	}
    }

    /**
     * Send a BYE packet, and cleanup
     */
    public void sendBye() throws IOException {
	if(_out!=null) {
	    writeHeader();
	    _dos.writeBytes("BYE\0");
	    _dos.writeInt(0);
	    _baos.writeTo(_out);
	    cleanup();
	}
    }

    /**
     * Add file into cache as url
     */
    public void add(String path, String url) 
	throws IOException, IllegalArgumentException
    {
	if(url==null || url.length()==0 || path==null || path.length()==0) {
	    throw new IllegalArgumentException("Zero length path or url");
	}

	connect();
	writeHeader();
	_dos.writeBytes("ADD\0");	
	// ulen+plen+2*'\0'+ 2*int
	_dos.writeInt(url.length()+path.length()+10); 
	_dos.writeInt(path.length()+1);
	_dos.writeInt(url.length()+1);
	_dos.writeBytes(path+"\0");
	_dos.writeBytes(url+"\0");
	_baos.writeTo(_out);

	readReply();
	switch(_reply_com) {
	case PushCacheProtocol.OK:
	    break;
	case PushCacheProtocol.ERR:
	    serverError();
	    break;
	default:
	    unexpectedReply();
	}
    }

    /**
     * Throws IllegalArgumentException iff com is not a null 
     * terminated string of the correct length
     */
    protected void checkCommand(String com) throws IllegalArgumentException {
	if(com.length()!=PushCacheProtocol.COMMAND_LEN) {
	    throw new IllegalArgumentException("Command \""+com+
					       "\" is wrong length");
	}
	if(com.charAt(3)!='\0') {
	    throw new IllegalArgumentException("Command \""+com+
					       "\" is not null terminated");
	}
    }

    /**
     * Send a packet with specified command and a url
     */
    public void urlCommand(String com, String url) 
	throws IOException, IllegalArgumentException 
    {
	if(url==null || url.length()==0) {
	    throw new IllegalArgumentException("Zero length url");
	}
	checkCommand(com);
	connect();
	writeHeader();
	_dos.writeBytes(com);
	_dos.writeInt(url.length()+1);
	_dos.writeBytes(url+"\0");
	_baos.writeTo(_out);
    }

    /**
     * Remove url from cache
     */
    public void del(String url) throws IOException {
	urlCommand("DEL\0",url);
	readReply();
	switch(_reply_com) {
	case PushCacheProtocol.OK:
	    break;
	case PushCacheProtocol.ERR:
	    serverError();
	    break;
	default:
	    unexpectedReply();
	}
    }

    /**
     * True iff url is present in cache
     */ 
    public boolean isPresent(String url) throws IOException {
	urlCommand("PRS\0", url);
	readReply();
	switch(_reply_com) {
	case PushCacheProtocol.OK:
	    if(_verbose>0) {
		System.err.println(url+" is present");
	    }
	    return(true);

	case PushCacheProtocol.NO:
	    if(_verbose>0) {
		System.err.println(url+" is not present");
	    }
	    return(false);

	case PushCacheProtocol.ERR:
	    serverError();
	    break;
	default:
	    unexpectedReply();
	}
	return(false);
    }

    /**
     * Send a simple packet with specified command
     */
    public void simpleCommand(String com) throws IOException {
	checkCommand(com);
	connect();
	writeHeader();
	_dos.writeBytes(com);
	_dos.writeInt(0);
	_baos.writeTo(_out);

	readReply();
	switch(_reply_com) {
	case PushCacheProtocol.OK:
	    break;
	case PushCacheProtocol.ERR:
	    serverError();
	    break;
	default:
	    unexpectedReply();
	}
    }

    /**
     * Send a NOP packet
     */
    public void nop() throws IOException {
	simpleCommand("NOP\0");
    }

    /**
     * Clean cache - remove all entries
     */
    public void clean() throws IOException {
	simpleCommand("CLN\0");
    }

    /**
     * For use as stand alone program - constructs Client with argv
     */
    public static void main(String[] argv) {
	try {
	    Client c=new Client(argv);
	}
	catch(Exception e) {
	    e.printStackTrace();
	}
    }
}
