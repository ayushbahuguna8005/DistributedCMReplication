package ServerOperationApp;

/**
* ServerOperationApp/ServerOperationIDLHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from ServerOperationIDL.idl
* Tuesday, June 12, 2018 10:17:40 PM EDT
*/

public final class ServerOperationIDLHolder implements org.omg.CORBA.portable.Streamable
{
  public ServerOperationApp.ServerOperationIDL value = null;

  public ServerOperationIDLHolder ()
  {
  }

  public ServerOperationIDLHolder (ServerOperationApp.ServerOperationIDL initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = ServerOperationApp.ServerOperationIDLHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    ServerOperationApp.ServerOperationIDLHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return ServerOperationApp.ServerOperationIDLHelper.type ();
  }

}
