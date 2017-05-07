# Shared Memory

The Shared Memory library allows for the creation of memory regions that may be simultaneously accessed by multiple Android processes or applications. Developed to overcome the Android 1MB IPC limitation, this Shared Memory library allows you to exchange larger amounts of data between your Android applications. 

<p align="center">
  <img src="https://github.com/NewtronLabs/SharedMemory/blob/master/Diagram.png" width="65%" height="65%" >
</p>

----

## How to Use 

### Setup

Include the below dependencies in your `build.gradle` project.

```gradle
allprojects {
    repositories {
        jcenter()
        maven { url "http://code.newtronlabs.com:8081/artifactory/libs-release-local" }
    }
}
```

In the `build.gradle` for your app.

```gradle
compile 'com.newtronlabs.sharedmemory:sharedmemory:1.0.3'
```

### Sharing Memory - Producer
From the application that wishes to shared its memory, allocate a shated memory region with a given name. 

```java
// Allocate 2MB
int sizeInBytes = 2*(1024*1024);
String regionName = "Test-Region";
ISharedMemory sharedMemory = SharedMemoryProducer.getInstance().allocate(regionName, sizeInBytes);
```

Write data to memory:
```java
byte[] strBytes = "Hello World!".getBytes();
sharedMemory.writeBytes(strBytes, 0, 0, strBytes.length);
```

Once an application has shared a memory region it can be accessed by other processes or application which are aware of it.

### Accessing Shared Memory - Consumer
In order for an application to access a region of memory shaered by an external application perform the following:

```java
// This is the application id of the application or process which shared the region.
String producerAppId = "com.newtronlabs.smproducerdemo";

// Name under wich the remote region was created.
String regionName = "Test-Region"

// Note: The remote application must have allocated a memory region with the same
//       name or this call will fail and return null.
IRemoteSharedMemory remoteMemory 
         = RemoteMemoryAdapter.getDefaultAdapter().getSharedMemory(context, producerAppId, regionName);

// Allocate memory to read shared content.
byte[] dataBytes = new byte[remoteMemory.getSize()];
String dataStr = new String(dataBytes);
Log.d("Newtron", "Memory Read:"+dataStr);
```

### Additional Samples
A set of more complex exmaples can be found in this repo's samples folders: **SmProducer** and **SmConsumer**. 

---
## License

Shared Memory binaries and source code can only be used in accordance with Freeware license. That is, freeware may be used without payment, but may not be modified. The developer of Shared Memory retains all rights to change, alter, adapt, and/or distribute the software. Shared Memory is not liable for any damages and/or losses incurred during the use of Shared Memory.

Users may not decompile, reverse engineer, pull apart, or otherwise attempt to dissect the source code, algorithm, technique or other information from the binary code of Shared Memory unless it is authorized by existing applicable law and only to the extent authorized by such law. In the event that such a law applies, user may only attempt the foregoing if: (1) user has contacted Newtron Labs to request such information and Newtron Labs has failed to respond in a reasonable time, or (2) reverse engineering is strictly necessary to obtain such information and Newtron Labs has failed to reply. Any information obtained by user from Newtron Labs may be used only in accordance to the terms agreed upon by Newtron Labs and in adherence to Newtron Labs confidentiality policy. Such information supplied by Newtron Labs and received by user shall not be disclosed to a third party or used to create a software substantially similar to the technique or expression of the Newtron Labs Shared Memory software.

*Patent Pending*

## Contact

contact@newtronlabs.com
