javac ServerOperationApp/*.java ServerOperationApp/ServerOperationIDLPackage/*.java serverpackage/*.java
javac client/*.java
start orbd -ORBInitialPort 1050
start java serverpackage.FrontEndServerStarter -ORBInitialPort 1050 -ORBInitialHost localhost