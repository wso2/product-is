#! /bin/sh
# ----------------------------------------------------------------------------
#  Copyright 2024 WSO2, LLC. https://www.wso2.com
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

OPENSSL_VERSION=3.2.1
LIBOQS_VERSION=0.12.0
OQS_PROVIDER_VERSION=0.8.0
TCNATIVE_VERSION=1.3.0
APR_VERSION=1.7.4

# Parameters
# $1: The directory where the OpenSSL configuration file should be copied
# $2: The extension of the library file
configure_openssl_conf() {
    echo "Configuring OpenSSL.conf"
    cd "$1/repository/resources/conf/templates/repository/conf/tomcat" \
        && wget -O openssl.cnf.j2 https://raw.githubusercontent.com/openssl/openssl/openssl-$OPENSSL_VERSION/apps/openssl.cnf \
        && echo "[provider_sect]" >> openssl.cnf.j2 \
        && echo "oqsprovider = oqsprovider_sect" >> openssl.cnf.j2 \
        && echo "[oqsprovider_sect]" >> openssl.cnf.j2 \
        && echo "activate = 1" >> openssl.cnf.j2 \
        && echo "module = $1/lib/oqsprovider.$2" >> openssl.cnf.j2 \
        && echo "[default_sect]" >> openssl.cnf.j2 \
        && echo "activate = 1" >> openssl.cnf.j2 \
        && echo "[openssl_init]" >> openssl.cnf.j2 \
        && echo "ssl_conf = ssl_sect" >> openssl.cnf.j2 \
        && echo "[ssl_sect]" >> openssl.cnf.j2 \
        && echo "system_default = system_default_sect" >> openssl.cnf.j2 \
        && echo "[system_default_sect]" >> openssl.cnf.j2 \
        && echo "Groups = {{transport.https.openssl.named_groups}}" >> openssl.cnf.j2
}

# Parameters
# $@: The list of dependencies to check
check_dependencies() {
    for cmd in "$@"; do
        if ! command -v "$cmd" >/dev/null 2>&1; then
            echo "[Error]: $cmd could not be found. Please install it before running this script."
            exit 1
        fi
    done
}

# Parameters
# $1: The directory where installation files should be downloaded
# $2: The directory where OpenSSL should be installed
install_openssl() {
    # Download and unpack OpenSSL
    cd "$1" \
        && wget https://www.openssl.org/source/openssl-$OPENSSL_VERSION.tar.gz \
        && tar -xf openssl-$OPENSSL_VERSION.tar.gz
    echo "Installing OpenSSL"
    cd "$1/openssl-$OPENSSL_VERSION" \
        && ./Configure --prefix="$2" --openssldir="$2" \
        && make \
        && make install_sw
}

# Parameters
# $1: The directory where installation files should be downloaded
# $2: The directory where OpenSSL is installed
# $3: The path to the libcrypto library
install_oqs_provider() {
    if [ -z "$2" ]; then
        openssl_root=""
    else
        openssl_root="-DOPENSSL_ROOT_DIR=$2"
    fi
    if [ -z "$3" ]; then
        openssl_crypto=""
    else
        openssl_crypto="-DOPENSSL_CRYPTO_LIBRARY=$3"
    fi

    # Download and unpack LibOQS
    cd "$1" \
        && wget https://github.com/open-quantum-safe/liboqs/archive/refs/tags/$LIBOQS_VERSION.tar.gz \
        && tar -xf $LIBOQS_VERSION.tar.gz
    echo "Installing LibOQS"
    cd "$1/liboqs-$LIBOQS_VERSION" \
        && cmake -DCMAKE_INSTALL_PREFIX="$1/liboqs" "$openssl_root" "$openssl_crypto" -S . -B _build \
        && cmake --build _build \
        && cmake --install _build

    # Download and unpack OQS Provider
    cd "$1" \
        && wget https://github.com/open-quantum-safe/oqs-provider/archive/refs/tags/$OQS_PROVIDER_VERSION.tar.gz \
        && tar -xf $OQS_PROVIDER_VERSION.tar.gz
    echo "Install OQS Provider"
    cd "$1/oqs-provider-${OQS_PROVIDER_VERSION}" \
        && liboqs_DIR="$1/liboqs" cmake "$openssl_root" "$openssl_crypto" -S . -B _build \
        && cmake --build _build
}

# Parameters
# $1: The directory where installation files should be downloaded
# $2: The directory where APR should be installed
install_apr() {
    # Download APR
    cd "$1" \
        && git clone https://github.com/apache/apr.git
    echo "Installing APR"
    cd "$1/apr" \
        && git checkout $APR_VERSION \
        && ./buildconf \
        && ./configure --prefix="$2" \
        && make \
        && make install
}

# Parameters
# $1: The directory where installation files should be downloaded
# $2: The directory where tcnative should be installed
# $3: The directory where OpenSSL is installed
# $4: The directory where APR is installed
install_tomcat_native() {
    if [ -z "$4" ]; then
        apr_config=""
    else
        apr_config="--with-apr=$4/bin/apr-1-config"
    fi
    if [ -z "$3" ]; then
        ssl=""
    else
        ssl="--with-ssl=$3"
    fi

    # Download tcnative
    cd "$1" \
        && wget https://github.com/apache/tomcat-native/archive/refs/tags/$TCNATIVE_VERSION.tar.gz \
        && tar -xf $TCNATIVE_VERSION.tar.gz
    echo "Installing tcnative"
    cd "$1/tomcat-native-$TCNATIVE_VERSION/native" \
        && git clone https://github.com/apache/apr.git \
        && ./buildconf --with-apr="$1/tomcat-native-$TCNATIVE_VERSION/native/apr" \
        && ./configure "$apr_config" "$ssl" --prefix="$2" \
        && make \
        && make install

    echo "Replacing symlinks with binaries"
    for file in "$2"/lib/libtcnative-1.*; do
        if [ -L "$file" ]; then
            target=$(readlink -f "$file")
            # Create a copy of the original file
            cp "$target" "$file".copy
            mv "$file".copy "$file"
            echo "Copied $target to replace $file"
        fi
    done
}

# =============================== Main Script =====================================================

# Exit immediately if a command exits with a non-zero status
set -e

# Check the OS type
if [ "$(uname)" = "Darwin" ]; then
    LIBEXT="dylib"
else
    LIBEXT="so"
fi

# Get standard environment variables
PRGDIR=$(dirname "$PRG")

# Only set CARBON_HOME if not already set
[ -z "$CARBON_HOME" ] && CARBON_HOME=$(cd "$PRGDIR/.." ; pwd)

# Check if $CARBON_HOME has spaces
if echo "$CARBON_HOME" | grep -q ' '; then
    echo "[Error]: Please make sure the absolute path of WSO2 Identity Server does not contain spaces for successful execution of this script."
    exit 1
fi

# Check if JAVA_HOME is set
if [ -z "$JAVA_HOME" ]; then
    echo "[Error]: JAVA_HOME is not set. Please set it before running this script."
    exit 1
fi

BUILD_OPENSSL=false
BUILD_PQCLIB=false

# Parse command line arguments
for arg in "$@"; do
    if [ "$arg" = "--build_openssl" ] || [ "$arg" = "-build_openssl" ] || [ "$arg" = "build_openssl" ]; then
        BUILD_OPENSSL=true
        echo "[Mode]: Building OpenSSL"
    elif [ "$arg" = "--build_pqclib" ] || [ "$arg" = "-build_pqclib" ] || [ "$arg" = "build_pqclib" ]; then
        BUILD_PQCLIB=true
        echo "[Mode]: Building OQS Provider"
    fi
done

# Check for build dependencies
check_dependencies make cmake wget tar gcc autoconf
# Check for Perl on RHEL-based systems
if [ -f "/etc/redhat-release" ]; then
    check_dependencies perl
fi

if [ $BUILD_OPENSSL = false ]; then
    OPENSSL_VERSION=$(openssl version | awk '{print $2}')
    case "$OPENSSL_VERSION" in
        3.*)
            echo "OpenSSL version $OPENSSL_VERSION"
            ;;
        *)
            echo "[Error]: Requires OpenSSL version 3.0.0 or higher. Found version $OPENSSL_VERSION"
            exit 1
            ;;
    esac
    if [ "$(uname)" != "Darwin" ]; then
        # Check if libssl-dev is installed
        if [ -f "/etc/debian_version" ]; then
            if ! dpkg -s libssl-dev > /dev/null 2>&1; then
                echo "[Error]: libssl-dev is not installed. Please install it before running this script."
                exit 1
            fi
        else
            if ! rpm -q openssl-devel > /dev/null 2>&1; then
                echo "[Error]: openssl-devel is not installed. Please install it before running this script."
                exit 1
            fi
        fi
        # Check if libapr1-dev is installed
        if [ -f "/etc/debian_version" ]; then
            if ! dpkg -s libapr1-dev > /dev/null 2>&1; then
                echo "[Error]: libapr1-dev is not installed. Please install it before running this script."
                exit 1
            fi
        else
            if ! rpm -q apr-devel > /dev/null 2>&1; then
                echo "[Error]: apr-devel is not installed. Please install it before running this script."
                exit 1
            fi
        fi
    fi
    check_dependencies apr-1-config
fi

TMP_DIR="$CARBON_HOME"/openssl-tmp
if [ -d "$TMP_DIR" ]; then
    rm -rf "$TMP_DIR"
fi
mkdir -p "$TMP_DIR"

# =============================== Install OpenSSL ================================================

if [ $BUILD_OPENSSL = true ]; then
    OPENSSL_INSTALL_DIR="$CARBON_HOME/native/openssl"
    if [ ! -d "$OPENSSL_INSTALL_DIR" ]; then
        install_openssl "$TMP_DIR" "$OPENSSL_INSTALL_DIR"
    else
        echo "OpenSSL is already installed"
    fi
elif [ "$(uname)" = "Darwin" ]; then
    if [ "$(uname -m)" = "arm64" ]; then
        # For M1 (ARM) architecture
        OPENSSL_INSTALL_DIR=$(ls -d '/opt/homebrew/Cellar/openssl@3/'*)
    else
        # For Intel (x86_64) architecture
        OPENSSL_INSTALL_DIR=$(ls -d '/usr/local/Cellar/openssl@3/'*)
    fi
fi

# =============================== Install OQS Provider ============================================

if [ $BUILD_PQCLIB = true ]; then
    if [ -d "$OPENSSL_INSTALL_DIR/lib64" ]; then
        OPENSSL_LIB_DIR="$OPENSSL_INSTALL_DIR/lib64"
    else
        OPENSSL_LIB_DIR="$OPENSSL_INSTALL_DIR/lib"
    fi

    # Check if OQS Provider is already installed
    if [ ! -f "$CARBON_HOME/lib/oqsprovider.$LIBEXT" ]; then
        if [ $BUILD_OPENSSL = true ] || [ "$(uname)" = "Darwin" ]; then
            install_oqs_provider "$TMP_DIR" "$OPENSSL_INSTALL_DIR" "$OPENSSL_LIB_DIR/libcrypto.$LIBEXT"
        else
            install_oqs_provider "$TMP_DIR"
        fi
        mv "$TMP_DIR/oqs-provider-$OQS_PROVIDER_VERSION/_build/lib/oqsprovider.$LIBEXT" "$CARBON_HOME/lib"
    else
        echo "OQS Provider is already installed"
    fi
fi

# =============================== Install APR ====================================================

if [ $BUILD_OPENSSL = true ]; then
    rm -rf "$CARBON_HOME/native/apr"
    install_apr "$TMP_DIR" "$CARBON_HOME/native/apr"
fi

# ============================== Install tcnative ================================================

if [ $BUILD_OPENSSL = true ]; then
    install_tomcat_native "$TMP_DIR" "$CARBON_HOME" "$OPENSSL_INSTALL_DIR" "$CARBON_HOME/native/apr"
elif [ ! -e "$CARBON_HOME/lib/libtcnative-1.$LIBEXT" ]; then
    if [ "$(uname)" = "Darwin" ]; then
        install_tomcat_native "$TMP_DIR" "$CARBON_HOME" "$OPENSSL_INSTALL_DIR"
    else
        install_tomcat_native "$TMP_DIR" "$CARBON_HOME"
    fi
else
    echo "tcnative is already installed"
fi

# ================================ Configure OpenSSL ==============================================

if [ $BUILD_PQCLIB = true ]; then
    configure_openssl_conf "$CARBON_HOME" $LIBEXT
    echo "Post-quantum security mode is installed successfully"
fi

echo "Cleaning up"
rm -rf "$TMP_DIR"
