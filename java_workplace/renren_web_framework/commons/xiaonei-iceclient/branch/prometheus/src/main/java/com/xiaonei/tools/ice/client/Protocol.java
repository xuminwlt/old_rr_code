package com.xiaonei.tools.ice.client;

final public class Protocol
{
    //
    // Size of the Ice protocol header
    //
    // Magic number (4 bytes)
    // Protocol version major (Byte)
    // Protocol version minor (Byte)
    // Encoding version major (Byte)
    // Encoding version minor (Byte)
    // Message type (Byte)
    // Compression status (Byte)
    // Message size (Int)
    //
    public final static int headerSize = 14;

    //
    // The magic number at the front of each message
    //
    public final static byte magic[] = { 0x49, 0x63, 0x65, 0x50 };      // 'I', 'c', 'e', 'P'

    //
    // The current Ice protocol and encoding version
    //
    public final static byte protocolMajor = 1;
    public final static byte protocolMinor = 0;
    public final static byte encodingMajor = 1;
    public final static byte encodingMinor = 0;

    //
    // The Ice protocol message types
    //
    public final static byte requestMsg = 0;
    public final static byte requestBatchMsg = 1;
    public final static byte replyMsg = 2;
    public final static byte validateConnectionMsg = 3;
    public final static byte closeConnectionMsg = 4;

    public final static byte[] requestHdr =
    {
        Protocol.magic[0],
        Protocol.magic[1],
        Protocol.magic[2],
        Protocol.magic[3],
        Protocol.protocolMajor,
        Protocol.protocolMinor,
        Protocol.encodingMajor,
        Protocol.encodingMinor,
        Protocol.requestMsg,
        (byte)0, // Compression status.
        (byte)0, (byte)0, (byte)0, (byte)0, // Message size (placeholder).
        (byte)0, (byte)0, (byte)0, (byte)0  // Request ID (placeholder).
    };

    public final static byte[] requestBatchHdr =
    {
        Protocol.magic[0],
        Protocol.magic[1],
        Protocol.magic[2],
        Protocol.magic[3],
        Protocol.protocolMajor,
        Protocol.protocolMinor,
        Protocol.encodingMajor,
        Protocol.encodingMinor,
        Protocol.requestBatchMsg,
        0, // Compression status.
        (byte)0, (byte)0, (byte)0, (byte)0, // Message size (placeholder).
        (byte)0, (byte)0, (byte)0, (byte)0  // Number of requests in batch (placeholder).
    };

    public final static byte[] replyHdr =
    {
        Protocol.magic[0],
        Protocol.magic[1],
        Protocol.magic[2],
        Protocol.magic[3],
        Protocol.protocolMajor,
        Protocol.protocolMinor,
        Protocol.encodingMajor,
        Protocol.encodingMinor,
        Protocol.replyMsg,
        (byte)0, // Compression status.
        (byte)0, (byte)0, (byte)0, (byte)0 // Message size (placeholder).
    };

}
