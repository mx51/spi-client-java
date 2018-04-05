# SPI Client Java

This is the Java Client Library for Assembly Payments' In-Store Integration.

# Usage

The sections below explain how to use this library in your project, depending on what build tool you use.

Library version is henceforth referred to as 'X.Y.Z', to find out what release versions are available, check the [tags](https://github.com/AssemblyPayments/spi-client-java/releases).

## Maven/Gradle

Easiest way to import the library into your project is using the Maven repository:

1. Add JCenter repository 
    - URL: https://jcenter.bintray.com/
2. Import dependency into your project: 
    - GroupId: `com.assemblypayments`
    - ArtifactId: `spi-client-java`
    - Version: `X.Y.Z`
    
See specific examples for build tools below.

### Gradle example

Declare the repository:

```
repositories {
    jcenter()
}
```

And use the library as a dependency:

```
dependencies {
    compile 'com.assemblypayments:spi-client-java:X.Y.Z'
}
```

### Maven example

Configure the repository to be used for dependency resolution:

```
<repositories>
    <repository>
        <id>jcenter</id>
        <url>https://jcenter.bintray.com/</url>
    </repository>
</repositories>
```

And use the library as a dependency:

```
<dependencies>
    <dependency>
        <groupId>com.assemblypayments</groupId>
        <artifactId>spi-client-java</artifactId>
        <version>X.Y.Z</version>
    </dependency>
</dependencies>
```

## Ant and others

For builds that cannot use the Maven repository, you can download a ZIP distribution from the [downloads](https://github.com/AssemblyPayments/spi-client-java/releases) section, look for a file `spi-client-java-X.Y.Z.zip` with the latest version.

Alternatively, you can generate such a ZIP distribution manually by running the following:

```
./gradlew client:distZip
```

The output can be found in `./client/build/distributions` after the command completes.

# Disclaimer

This source code is provided “as is“ or “as available“ and Assembly makes no representations or warranties, express or implied, regarding the source code, that the source code will meet your requirements, or that this source code will be error-free. Assembly expressly disclaims any and all express and implied warranties, including, but not limited to, the implied warranties of merchantability, fitness for a particular purpose, and non-infringement. Without limiting the generality of the foregoing, Assembly does not warrant, endorse, guarantee, or assume responsibility for this source code.   

In no event shall Assembly be liable for any direct, indirect, incidental, special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability, whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use of this source code, even if advised of the possibility of such damage.
