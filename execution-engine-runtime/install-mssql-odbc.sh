# From: https://learn.microsoft.com/en-us/sql/connect/odbc/linux-mac/installing-the-microsoft-odbc-driver-for-sql-server

case $(uname -m) in
    x86_64)   architecture="amd64" ;;
    arm64)   architecture="arm64" ;;
    aarch64)   architecture="arm64" ;; # added by Adam :)
    *) architecture="unsupported" ;;
esac
if [[ "unsupported" == "$architecture" ]];
then
    echo "Alpine architecture $(uname -m) is not currently supported.";
    exit;
fi


#Download the desired package(s)
curl -O https://download.microsoft.com/download/3/5/5/355d7943-a338-41a7-858d-53b259ea33f5/msodbcsql18_18.3.1.1-1_$architecture.apk
curl -O https://download.microsoft.com/download/3/5/5/355d7943-a338-41a7-858d-53b259ea33f5/mssql-tools18_18.3.1.1-1_$architecture.apk


#(Optional) Verify signature, if 'gpg' is missing install it using 'apk add gnupg':
# curl -O https://download.microsoft.com/download/3/5/5/355d7943-a338-41a7-858d-53b259ea33f5/msodbcsql18_18.3.1.1-1_$architecture.sig
# curl -O https://download.microsoft.com/download/3/5/5/355d7943-a338-41a7-858d-53b259ea33f5/mssql-tools18_18.3.1.1-1_$architecture.sig
#
# curl https://packages.microsoft.com/keys/microsoft.asc  | gpg --import -
# gpg --verify msodbcsql18_18.3.1.1-1_amd64.sig msodbcsql18_18.3.1.1-1_$architecture.apk
# gpg --verify mssql-tools18_18.3.1.1-1_amd64.sig mssql-tools18_18.3.1.1-1_$architecture.apk

RUN curl https://packages.microsoft.com/keys/microsoft.asc | apt-key add -
RUN curl https://packages.microsoft.com/config/ubuntu/18.04/prod.list > /etc/apt/sources.list.d/mssql-release.list
RUN apt-get update -y
RUN ACCEPT_EULA=Y apt-get install -y msodbcsql17 unixodbc-dev mssql-tools


#Install the package(s)
sudo apk add --allow-untrusted msodbcsql18_18.3.1.1-1_$architecture.apk
sudo apk add --allow-untrusted mssql-tools18_18.3.1.1-1_$architecture.apk
