#!/bin/bash


# A very crude script to setup debian6-19-04-2012 to use Realtek RRTL8188CUS based Wireless LAN Adapters

# Update: 20/05/2012 - It is no longer necessary to edit the script to setup the network SSID & PASSWORD
# The script will ask you to input these values when it needs them.

# Update: 25/05/2012 - I have added an option to enable the wifi adapter to be hotpluggable.
# i.e. you can remove the adapter while the Pi is powered on and then plug it back in later
# and the wifi will automatically re-install and reconnect to the wireless network.

# Update: 08/06/2012 - I have compiled a new driver that overcomes the problems found using rpi-update
# recently. This driver also does not require additional firmware so is a little simpler to install.
# The script will detect if you have an ethernet connection and update the Pi's firmware and software
# automatically and download and install the new driver automatically if you have.
# If you do not have a network connection it will expect the old driver and it's firmware to be installed
# in the /boot directory of the SD card before running the script. The driver will be installed and the
# wifi started and then the Pi's firmware and software will be downloaded and installed. Finally the new
# driver will be installed to ensure the wifi continues working with the updated software.

# Update: 11/06/2012 - Updated driver to take care of latest rpi-updates

# Update: 17/06/2012 - Updated driver to take care of latest rpi-updates

# Update: 24/06/2012 - Updated to allow selecting a connection to an unsecured network or a secured network
# with either WEP or WPA/WPA2 security. Added option to enable DHCP to be installed or not as required.

# Update: 06/07/2012 - Major update. The script will now install, upgrade or repair the wifi driver.
# It will allow more than one wifi adapter to be installed, adding the rtl8188cus drive if another
# driver is installed, or add a second rtl8188cus adapter if one is already installed.
# The script can be run on a system with the rtl8188cus already installed to upgrade/repair the driver
# if it has been broken by a software upgrade.
#

#Update: 08/08/2012 - Updated to be able to use the script to add/update the wifi driver on XBian

echo
echo "This script will install the driver for Realtek RTL8188CUS based wifi adapters."
echo
echo "For all images other than XBian the script will also update the software to the"
echo "latest version using apt-get update, apt-get upgrade and rpi-update."
echo
echo "1. It can install a new driver if you do not already have the rtl8188cus driver"
echo "   installed and have no other wifi adapter installed."
echo "2. It can install a wifi adapter using the rtl8188cus driver if you have a wifi"
echo "   adapter using a different driver already installed."
echo "3. If the driver is already installed it will update the driver and software, or"
echo "   allow you to add an different wifi adapter using the rtl8188cus driver so you"
echo "   can switch between them if you want to, e.g. unplug one and plug in another,"
echo "   or even connect two wifi adapters at the same time."
echo "4. It can repair a broken driver. e.g. if you have updated the software and the"
echo "   wifi has stopped working it will update the driver to a working version if"
echo "   one is available."
echo
echo "If you are installing your first wifi adapter or adding a new one do not plug it"
echo "in until told to do so. If you have a wifi adapter already installed and now it"
echo "is not working due to a recent software update unplug it now and plug it back in"
echo "when the script tells you to."
echo
echo "If you don't have a working wifi adapter but do have a wired network with access"
echo "to the internet connect the internet cable. Files needed for the installation"
echo "will be downloaded from the internet."
echo
echo "If you have a working wifi adapter and it is plugged in do not unplug it. It"
echo "will be used to download files needed for the installation from the internet."
echo
read -p "Press any key to continue..." -n1 -s
echo
echo

# First check current wifi configuration

DRIVER_INSTALLED=1
ADAPTER_NUMBER=0
INTERNET_CONNECTED=1
EXITSTATUS=1

# check the rtl8188cus driver is installed

if [ -e "/lib/modules/3.1.9+/kernel/drivers/net/wireless/8192cu.ko" ] || [ -e "/lib/modules/3.2.21+/kernel/drivers/net/wireless/8192cu.ko" ]; then
	DRIVER_INSTALLED=0
	echo -n "An RTL8188CUS driver module is installed"
	lsmod > temp.tmp 2> /dev/null
	if grep -q "8192cu" temp.tmp ; then
		echo " and loaded."
	else
		echo " but is not loaded."
	fi
else
	echo "The RTL8188CUS driver module is not installed."
	DRIVER_INSTALLED=1
fi

# check the /etc/network/interfaces file for configured wifi adapters

TOTAL_COUNT=$(grep -c "wlan" /etc/network/interfaces)

while true; do

# check number of lines with wlanX
	CONFIG=0
	CONFIG1=0
	COUNT=$(grep -c "wlan$ADAPTER_NUMBER" /etc/network/interfaces)
	if [ ${COUNT} != 0 ]; then

# check for line with "iface wlanX" at start. If it exists there must be a line with "auto wlanX"
# and/or "allow-hotplug wlanX".  if the line doesn't exist other lines shouldn't exist and any lines
# with wlanX should be commented out.

		if grep -q "^iface wlan$ADAPTER_NUMBER" /etc/network/interfaces ; then
			let CONFIG=1
			if grep -q -x "auto wlan$ADAPTER_NUMBER" /etc/network/interfaces ; then
				let CONFIG=CONFIG+1
			fi
			if grep -q -x "allow-hotplug wlan$ADAPTER_NUMBER" /etc/network/interfaces ; then
				let CONFIG=CONFIG+1
			fi
		fi

# check for lines commented out that including wlanX

		let CONFIG1=$(grep "^#" /etc/network/interfaces | grep -c "wlan${ADAPTER_NUMBER}")  2> /dev/null
		let CONFIG1=CONFIG1+CONFIG
	fi

	if [ ${CONFIG} == 2 ] || [ ${CONFIG} == 3 ]; then
		echo -n "wlan$ADAPTER_NUMBER is configured" >> installed_wifi1.txt
		ifconfig wlan$ADAPTER_NUMBER > temp.tmp 2> /dev/null
		if grep -q "wlan$ADAPTER_NUMBER" temp.tmp ; then
			if grep -q "inet addr:" temp.tmp ; then
				echo ", installed, and has a network connection." >> installed_wifi1.txt
			else
				echo " and installed, but has no network connection." >> installed_wifi1.txt
			fi
		else
			echo " but is not installed." >> installed_wifi1.txt
		fi
		let ADAPTER_NUMBER=ADAPTER_NUMBER+1
	else
		if grep -q "wlan$ADAPTER_NUMBER" /etc/network/interfaces ; then
			echo
			echo "The file /etc/network/interfaces appears to have been edited. The script will"
			echo "abort to avoid any problems that may occur if the installation continues."
			echo
			echo "Please check the file /etc/network/interfaces. A very basic setup for wlan$ADAPTER_NUMBER"
			echo "should look something like:-"
			echo
			echo "allow-hotplug wlan$ADAPTER_NUMBER             <--this line is optional"
			echo 
			echo "auto wlan$ADAPTER_NUMBER                                         unsecured network-----\\"
			echo "                           /--WPA/WPA2 network     WEP network------\\     |"
			echo "iface wlan$ADAPTER_NUMBER inet dhcp     |                                          |    |"
			echo "wpa-ssid \"SSID\"        <-/|     <--may be 'wireless-essid SSID'   <-/| <-/|"
			echo "wpa-psk \"PASSWORD\"     <-/      <--may be 'wireless-key PASSWORD' <-/     |"
			echo "                                           or blank.                   <-/"
			echo
			echo "wpa-config /etc/wpa_supplicant/wpa_supplicant.conf  <-may replace the two 'wpa'"
			echo "                                                        lines."
			echo
			echo "Aborting the installation script."
			echo
			exit
		else
			if [ ${ADAPTER_NUMBER} == 1 ]; then
				echo "You have $ADAPTER_NUMBER wifi adapter configured." >> installed_wifi.txt
			else
				echo "You have $ADAPTER_NUMBER wifi adapters configured." >> installed_wifi.txt
			fi
		fi
	echo >> installed_wifi1.txt
	break
	fi
done

echo
cat  installed_wifi.txt 2> /dev/null
cat  installed_wifi1.txt 2> /dev/null

rm  installed_wifi.txt 2> /dev/null
rm  installed_wifi1.txt 2> /dev/null

# check if there is an internet connection

COUNT=0
while [ $COUNT -lt 5 ] && [ ${INTERNET_CONNECTED} != 0 ]; do
#	ping -c 1 pool.ntp.org >/dev/null 2>&1
	ping -c 1 ntp0.zen.co.uk >/dev/null 2>&1
	INTERNET_CONNECTED=$?
	if [ ${INTERNET_CONNECTED} != 0 ]; then
		let COUNT=COUNT+1
	fi
done

if [ ${INTERNET_CONNECTED} == 0 ]; 	then
	echo "The Pi has an internet connection."
	echo
	echo "Any files needed for the installation/upgrade will be downloaded from the"
	echo "Internet."
	echo
else
	echo "The Pi has no Internet connection."
	echo
	echo "A basic installation will be made to enable an internet connection using the"
	echo "wifi. This will then allow the remaining files required to be downloaded from"
	echo "the internet. Any files needed for the basic wifi installation need to be in"
	echo "the /boot directory of the SD card for the installation to complete."
	echo
	echo "If you are unsure of the files required for the installation the script will"
	echo "notify you of any files it cannot find that are needed then the script will"
	echo "abort. You must then copy the files to the SD card /boot directory and re-run"
	echo "the script. If you're using a Windows system to generate the SD card the /boot"
	echo "directory is the one you see when viewing the SD card with Windows Explorer."
	echo
	echo "To copy the files you can either shut down the Pi, remove the SD card and copy"
	echo "the files needed to the SD card using the system you used to copy the image to"
	echo "the SD card, or you can copy the files to a usb stick or similar and then mount"
	echo "the usb stick on the Pi and copy the files from the USB stick to the /boot"
	echo "directory of the Pi."
	echo
	read -p "Press any key to continue..." -n1 -s
	echo
	echo
fi

# check the image has the wpa_supplicant and wireless-tools packages installed

rm driver_file.txt 2> /dev/null
uname -v > linux_version.txt

EXITSTATUS=0
if [ ! -f /sbin/wpa_supplicant ] || [ ! -f /sbin/iwconfig ] || [ ! -f /usr/bin/unzip ]; then
	echo
	echo "The image you are using needs wpa_supplicant and wireless-tools installing."
	echo
	if [ ${INTERNET_CONNECTED} == 0 ]; then
		echo
		echo "Updating the software packages list."
		echo
		EXITSTATUS=-1
		until [ ${EXITSTATUS} == 0 ]; do
			apt-get update
			EXITSTATUS=$?
		done
		echo
		echo "Installing the wireless-tools and wpasupplicant packages required for the wifi to"
		echo "operate."
		echo
		EXITSTATUS=-1
		until [ ${EXITSTATUS} == 0 ]; do
			apt-get install -y unzip wireless-tools wpasupplicant 2> /dev/null
			EXITSTATUS=$?
		done
	else
		echo
		echo "Installing the wireless-tools and wpasupplicant packages required for the wifi to"
		echo "operate."
		echo

# check packages needed are on the SD card in the /bootdirectory and copy to the home directory

		if ! grep -q "#107 PREEMPT Sun Jun 10 15:57:56 BST 2012" linux_version.txt ; then

# install the unzip package if needed for the wifi installation
			EXITSTATUS=0
			if [ ! -f /usr/bin/unzip ]; then
				if [ -f /boot/unzip_6.0-7_armhf.deb ]; then
					cp /boot/unzip_6.0-7_armhf.deb ./

					dpkg -i unzip_6.0-7_armhf.deb
				else
					EXITSTATUS=1
				fi
			fi

# install the wireless-tools package if needed for the wifi installation

			if [ ${EXITSTATUS} == 0 ] && [ ! -f /sbin/iwconfig ]; then
				if [ -f /boot/libiw30_30~pre9-8_armhf.deb ] && [ -f /boot/wireless-tools_30~pre9-8_armhf.deb ]; then
					cp /boot/libiw30_30~pre9-8_armhf.deb ./
					cp /boot/wireless-tools_30~pre9-8_armhf.deb ./

					dpkg -i libiw30_30~pre9-8_armhf.deb wireless-tools_30~pre9-8_armhf.deb
				else
					EXITSTATUS=1
				fi
			fi

# install the wpasupplicant package if needed for the wifi installation
#echo "test0a"
			if [ ${EXITSTATUS} == 0 ] && [ ! -f /sbin/wpa_supplicant ]; then
#echo "test0b"
				if [ -f /boot/libnl-3-200_3.2.7-4_armhf.deb ] && [ -f /boot/libnl-genl-3-200_3.2.7-4_armhf.deb ] && [ -f /boot/libpcsclite1_1.8.4-1_armhf.deb ] && [ -f /boot/wpasupplicant_1.0-2_armhf.deb ]; then
#echo "test0c"
					cp /boot/libnl-3-200_3.2.7-4_armhf.deb ./
					cp /boot/libnl-genl-3-200_3.2.7-4_armhf.deb ./
					cp /boot/libpcsclite1_1.8.4-1_armhf.deb ./
					cp /boot/wpasupplicant_1.0-2_armhf.deb ./

					if ! grep -q "#1 PREEMPT Wed Jun 6 16:26:14 CEST 2012" linux_version.txt ; then
#echo "test0d"
						dpkg -i libnl-3-200_3.2.7-4_armhf.deb libnl-genl-3-200_3.2.7-4_armhf.deb libpcsclite1_1.8.4-1_armhf.deb wpasupplicant_1.0-2_armhf.deb
					else
#echo "test0e"

						if [ -f /boot/dbus_1.6.0-1_armhf.deb ] && [ -f /boot/libdbus-1-3_1.6.0-1_armhf.deb ] && [ -f /boot/libexpat1_2.1.0-1_armhf.deb ] && [ -f /boot/libsystemd-login0_44-3_armhf.deb ]; then
#echo "test0f"
							cp /boot/dbus_1.6.0-1_armhf.deb ./
							cp /boot/libdbus-1-3_1.6.0-1_armhf.deb ./
							cp /boot/libexpat1_2.1.0-1_armhf.deb ./
							cp /boot/libsystemd-login0_44-3_armhf.deb ./

							dpkg -i libnl-3-200_3.2.7-4_armhf.deb libnl-genl-3-200_3.2.7-4_armhf.deb libpcsclite1_1.8.4-1_armhf.deb wpasupplicant_1.0-2_armhf.deb dbus_1.6.0-1_armhf.deb libdbus-1-3_1.6.0-1_armhf.deb libexpat1_2.1.0-1_armhf.deb libsystemd-login0_44-3_armhf.deb
						else
#echo "test0g"
							EXITSTATUS=1
						fi
					fi
				else
#echo "test0h"
					EXITSTATUS=1
				fi
			fi

# if any files are not in the /boot directory of the SD card generate a list to display to the user
#echo "test0i"

			if [ ${EXITSTATUS} != 0 ]; then
#echo "test0j"
				if [ ! -f /usr/bin/unzip ] && [ ! -f /boot/unzip_6.0-7_armhf.deb ]; then
					echo "unzip_6.0-7_armhf.deb" >> driver_file.txt
				fi
				if [ ! -f /sbin/iwconfig ] && [ ! -f /boot/libiw30_30~pre9-8_armhf.deb ]; then
					echo "libiw30_30~pre9-8_armhf.deb" >> driver_file.txt
				fi
				if [ ! -f /sbin/iwconfig ] && [ ! -f /boot/wireless-tools_30~pre9-8_armhf.deb ]; then
					echo "wireless-tools_30~pre9-8_armhf.deb" >> driver_file.txt
				fi
				if grep -q "#1 PREEMPT Wed Jun 6 16:26:14 CEST 2012" linux_version.txt ; then
#echo "test0k"
					if [ ! -f /sbin/wpa_supplicant ] && [ ! -f /boot/dbus_1.6.0-1_armhf.deb ]; then
						echo "dbus_1.6.0-1_armhf.deb" >> driver_file.txt
					fi
					if [ ! -f /sbin/wpa_supplicant ] && [ ! -f /boot/libdbus-1-3_1.6.0-1_armhf.deb ]; then
						echo "libdbus-1-3_1.6.0-1_armhf.deb" >> driver_file.txt
					fi
					if [ ! -f /sbin/wpa_supplicant ] && [ ! -f /boot/libexpat1_2.1.0-1_armhf.deb ]; then
						echo "libexpat1_2.1.0-1_armhf.deb" >> driver_file.txt
					fi
					if [ ! -f /sbin/wpa_supplicant ] && [ ! -f /boot/libsystemd-login0_44-3_armhf.deb ]; then
						echo "libsystemd-login0_44-3_armhf.deb" >> driver_file.txt
					fi
				fi
				if [ ! -f /sbin/wpa_supplicant ] && [ ! -f /boot/libnl-3-200_3.2.7-4_armhf.deb ]; then
					echo "libnl-3-200_3.2.7-4_armhf.deb" >> driver_file.txt
				fi
				if [ ! -f /sbin/wpa_supplicant ] && [ ! -f /boot/libnl-genl-3-200_3.2.7-4_armhf.deb ]; then
					echo "libnl-genl-3-200_3.2.7-4_armhf.deb" >> driver_file.txt
				fi
				if [ ! -f /sbin/wpa_supplicant ] && [ ! -f /boot/libpcsclite1_1.8.4-1_armhf.deb ]; then
					echo "libpcsclite1_1.8.4-1_armhf.deb" >> driver_file.txt
				fi
				if [ ! -f /sbin/wpa_supplicant ] && [ ! -f /boot/wpasupplicant_1.0-2_armhf.deb ]; then
					echo "wpasupplicant_1.0-2_armhf.deb" >> driver_file.txt
				fi
				rm *.deb 2> /dev/null

			else

				rm *.deb 2> /dev/null
				rm /boot/*.deb 2> /dev/null

				echo
			fi

		else

# install wpasupplicant on wheezy alpha

#echo "test0l"
			if [ -f /boot/libnl-3-200_3.2.7-4_armel.deb ] && [ -f /boot/libnl-genl-3-200_3.2.7-4_armel.deb ] && [ -f /boot/libpcsclite1_1.8.4-1_armel.deb ] && [ -f /boot/wpasupplicant_1.0-2_armel.deb ]; then
#echo "test0m"
				cp /boot/libnl-3-200_3.2.7-4_armel.deb ./
				cp /boot/libnl-genl-3-200_3.2.7-4_armel.deb ./
				cp /boot/libpcsclite1_1.8.4-1_armel.deb ./
				cp /boot/wpasupplicant_1.0-2_armel.deb ./

				dpkg -i libnl-3-200_3.2.7-4_armel.deb libnl-genl-3-200_3.2.7-4_armel.deb libpcsclite1_1.8.4-1_armel.deb wpasupplicant_1.0-2_armel.deb

				rm *.deb 2> /dev/null
				rm /boot/*.deb 2> /dev/null

				EXITSTATUS=0
			else
				if [ ! -f /boot/libnl-3-200_3.2.7-4_armel.deb ]; then
					echo "libnl-3-200_3.2.7-4_armel.deb" >> driver_file.txt
				fi
				if [ ! -f /boot/libnl-genl-3-200_3.2.7-4_armel.deb ]; then
					echo "libnl-genl-3-200_3.2.7-4_armel.deb" >> driver_file.txt
				fi
				if [ ! -f /boot/libpcsclite1_1.8.4-1_armel.deb ]; then
					echo "libpcsclite1_1.8.4-1_armel.deb" >> driver_file.txt
				fi
				if [ ! -f /boot/wpasupplicant_1.0-2_armel.deb ]; then
					echo "wpasupplicant_1.0-2_armel.deb" >> driver_file.txt
				fi
				rm *.deb 2> /dev/null
				EXITSTATUS=1
			fi
		fi
	fi
fi

# copy or download the basic driver files compatible with the installed Linux version

#echo "Test0"
if [ ${EXITSTATUS} == 0 ]; then
#echo "Test1"
	if grep -q "#52 Tue May 8 23:49:32 BST 2012\|#66 Thu May 17 16:56:20 BST 2012\|#90 Wed Apr 18 18:23:05 BST 2012" linux_version.txt ; then
#echo "Test1a"
		if [ ${INTERNET_CONNECTED} == 0 ]; then
			EXITSTATUS=-1
			until [ ${EXITSTATUS} == 0 ]; do
				wget http://www.electrictea.co.uk/rpi/8192cu.tar.gz 2> /dev/null
				EXITSTATUS=$?
				if [ ${EXITSTATUS} != 0 ]; then
					sleep 4
				fi
			done
			EXITSTATUS=-1
			until [ ${EXITSTATUS} == 0 ]; do
				wget ftp://ftp.dlink.com/Wireless/dwa130_revC/Drivers/dwa130_revC_drivers_linux_006.zip 2> /dev/null
				EXITSTATUS=$?
				if [ ${EXITSTATUS} != 0 ]; then
					sleep 4
				fi
			done
		else
			if [ -f /boot/8192cu.tar.gz ] && [ -f /boot/dwa130_revC_drivers_linux_006.zip ]; then
				mv /boot/8192cu.tar.gz ./
				mv /boot/dwa130_revC_drivers_linux_006.zip ./
				EXITSTATUS=0
			else
				if [ ! -f /boot/8192cu.tar.gz ]; then
					echo "8192cu.tar.gz" >> driver_file.txt
				fi
				if [ ! -f /boot/dwa130_revC_drivers_linux_006.zip ]; then
					echo "dwa130_revC_drivers_linux_006.zip" >> driver_file.txt
				fi
				EXITSTATUS=1
			fi
		fi
	else
#echo "Test2"
		if grep -q "#1 PREEMPT Wed Jun 6 16:26:14 CEST 2012\|#101 PREEMPT Mon Jun 4 17:19:44 BST 2012" linux_version.txt ; then
#echo "Test2a"
			if [ ${INTERNET_CONNECTED} == 0 ]; then
				EXITSTATUS=-1
				until [ ${EXITSTATUS} == 0 ]; do
					wget http://dl.dropbox.com/u/80256631/8192cu-20120607.tar.gz 2> /dev/null
					EXITSTATUS=$?
					if [ ${EXITSTATUS} != 0 ]; then
						sleep 4
					fi
				done
			else
				if [ -f /boot/8192cu-20120607.tar.gz ]; then
					mv /boot/8192cu-20120607.tar.gz ./ 2> /dev/null
					EXITSTATUS=0
				else
					echo "8192cu-20120607.tar.gz" >> driver_file.txt
					EXITSTATUS=1
				fi
			fi
		else
#echo "Test3"
			if grep -q "#107 PREEMPT Sun Jun 10 15:57:56 BST 2012\|#110 PREEMPT Wed Jun 13 11:41:58 BST 2012" linux_version.txt ; then
#echo "Test3a"
				if [ ${INTERNET_CONNECTED} == 0 ]; then
					EXITSTATUS=-1
					until [ ${EXITSTATUS} == 0 ]; do
						wget http://dl.dropbox.com/u/80256631/8192cu-20120611.tar.gz 2> /dev/null
						EXITSTATUS=$?
						if [ ${EXITSTATUS} != 0 ]; then
							sleep 4
						fi
					done
				else
					if [ -f /boot/8192cu-20120611.tar.gz ]; then
						mv /boot/8192cu-20120611.tar.gz ./ 2> /dev/null
						EXITSTATUS=0
					else
						echo "8192cu-20120611.tar.gz" >> driver_file.txt
						EXITSTATUS=1
					fi
				fi
			else
#echo "Test4"
				if grep -q "#122 PREEMPT Sun Jun 17 00:30:41 BST 2012\|#125 PREEMPT Sun Jun 17 16:09:36 BST 2012\|#128 PREEMPT Thu Jun 21 01:59:01 BST 2012\|#135 PREEMPT Fri Jun 22 20:39:30 BST 2012\|#138 PREEMPT Tue Jun 26 16:27:52 BST 2012" linux_version.txt ; then
#echo "Test4a"
					if [ ${INTERNET_CONNECTED} == 0 ]; then
						EXITSTATUS=-1
						until [ ${EXITSTATUS} == 0 ]; do
							wget http://dl.dropbox.com/u/80256631/8192cu-20120629.tar.gz 2> /dev/null
							EXITSTATUS=$?
							if [ ${EXITSTATUS} != 0 ]; then
								sleep 4
							fi
						done
					else
						if [ -f /boot/8192cu-20120629.tar.gz ]; then
							mv /boot/8192cu-20120629.tar.gz ./ 2> /dev/null
							EXITSTATUS=0
						else
							echo "8192cu-20120629.tar.gz" >> driver_file.txt
							EXITSTATUS=1
						fi
					fi
				else
#echo "Test5"
					if grep -q "#144 PREEMPT Sun Jul 1 12:37:10 BST 2012\|#149 PREEMPT Thu Jul 5 01:33:01 BST 2012\|#152 PREEMPT Fri Jul 6 18:47:16 BST 2012\|#155 PREEMPT Mon Jul 9 12:49:19 BST 2012\|#159 PREEMPT Wed Jul 11 19:54:53 BST 2012\|#162 PREEMPT Thu Jul 12 12:01:22 BST 2012\|#165 PREEMPT Fri Jul 13 18:54:13 BST 2012\|#168 PREEMPT Sat Jul 14 18:56:31 BST 2012\|#171 PREEMPT Tue Jul 17 01:08:22 BST 2012\|#174 PREEMPT Sun Jul 22 19:04:28 BST 2012" linux_version.txt ; then
#echo "Test5a"
						if [ ${INTERNET_CONNECTED} == 0 ]; then
							EXITSTATUS=-1
							until [ ${EXITSTATUS} == 0 ]; do
								wget http://dl.dropbox.com/u/80256631/8192cu-20120701.tar.gz 2> /dev/null
								EXITSTATUS=$?
								if [ ${EXITSTATUS} != 0 ]; then
									sleep 4
								fi
							done
						else
							if [ -f /boot/8192cu-20120701.tar.gz ]; then
								mv /boot/8192cu-20120701.tar.gz ./ 2> /dev/null
								EXITSTATUS=0
							else
								echo "8192cu-20120701.tar.gz" >> driver_file.txt
								EXITSTATUS=1
							fi
						fi
					else
#echo "Test6"
						if grep -q "#202 PREEMPT Wed Jul 25 22:11:06 BST 2012\|#242 PREEMPT Wed Aug 1 19:47:22 BST 2012\|#272 PREEMPT Tue Aug 7 22:51:44 BST 2012" linux_version.txt ; then
#echo "Test6a"
							if [ ${INTERNET_CONNECTED} == 0 ]; then
								EXITSTATUS=-1
								until [ ${EXITSTATUS} == 0 ]; do
									wget http://dl.dropbox.com/u/80256631/8192cu-20120726.tar.gz 2> /dev/null
									EXITSTATUS=$?
									if [ ${EXITSTATUS} != 0 ]; then
										sleep 4
									fi
								done
							else
								if [ -f /boot/8192cu-20120726.tar.gz ]; then
									mv /boot/8192cu-20120726.tar.gz ./ 2> /dev/null
									EXITSTATUS=0
								else
									echo "8192cu-20120726.tar.gz" >> driver_file.txt
									EXITSTATUS=1
								fi
							fi
						else
#echo "Test7"
							if grep -q "#1 Wed Aug 1 13:17:22 PDT 2012" linux_version.txt ; then
#echo "Test7a"
								if [ ${INTERNET_CONNECTED} == 0 ]; then
									EXITSTATUS=-1
									until [ ${EXITSTATUS} == 0 ]; do
										wget http://dl.dropbox.com/u/80256631/xbian-8192cu.tar.gz 2> /dev/null
										EXITSTATUS=$?
										if [ ${EXITSTATUS} != 0 ]; then
											sleep 4
										fi
									done
								else
									if [ -f /boot/xbian-8192cu.tar.gz ]; then
										mv /boot/xbian-8192cu.tar.gz ./ 2> /dev/null
										EXITSTATUS=0
									else
										echo "xbian-8192cu.tar.gz" >> driver_file.txt
										EXITSTATUS=1
									fi
								fi
							else
#echo "Test8"
								echo
								echo -n "Unrecognised software version: "
								uname -a
								echo
								if [ ${INTERNET_CONNECTED} != 0 ]; then
									echo "The script may be out of date. Download the latest version of the script from"
									echo "\"http://dl.dropbox.com/u/80256631/install-rtl8188cus-latest.sh\""
									echo
									echo "Compare the new script with the one you already have. If they are different"
									echo "replace the script in the /boot directory of the SD card with the new one and"
									echo "then re-run the script."
								else
									echo "Downloading the latest script."
									echo
									EXITSTATUS=1
									until [ ${EXITSTATUS} == 0 ]; do
										wget http://dl.dropbox.com/u/80256631/install-rtl8188cus-latest.sh 2> /dev/null
										EXITSTATUS=$?
										if [ ${EXITSTATUS} != 0 ]; then
											sleep 4
										fi
									done
									if cmp -s ./install-rtl8188cus-latest.sh /boot/install-rtl8188cus-latest.sh 2> /dev/null ; then
										rm install-rtl8188cus-latest.sh
										echo "The script you're using is the latest version."
									else
										mv install-rtl8188cus-latest.sh /boot
										chmod +x /boot/install-rtl8188cus-latest.sh
										echo "The script has changed. The new script has been copied to the /boot directory"
										echo "of the SD card. Run the new script after this one has aborted."
									fi
								fi
								echo
								echo "Aborting the rtl8188cus installation script."
								echo
								echo
								exit
							fi
						fi
					fi
				fi
			fi
		fi
	fi
fi

if [ -f driver_file.txt ]; then
	echo
	echo  "The file(s) "
	cat driver_file.txt
	echo "must be in the /boot directory of the SD card, or a wired internet connection"
	echo "must be made to the Pi for the installation to continue."
	echo
	echo "Copy the file(s) to the /boot directory of the SD card or connect to a wired"
	echo "internet connection and re-run the script."
	echo
	echo "Aborting the rtl8188cus installation script."
	echo

	rm driver_file.txt
	rm linux_version.txt

	exit
fi

echo
echo "The wifi driver for your current Linux version will now be installed/re-installed"
echo "and the necessary files will be configured as required."
sleep 2

# check if the basic driver and firmware are being loaded. if yes first install the firmware

if [ -f dwa130_revC_drivers_linux_006.zip ]; then
	unzip -q dwa130_revC_drivers_linux_006.zip > /dev/null 2>&1
	mkdir -p /usr/local/lib/firmware/RTL8192U
	cp rtl8192u_linux_2.6.0006.1031.2008/firmware/RTL8192U/* /usr/local/lib/firmware/RTL8192U

# delete the firmware files in the home directory - the firmware has been installed so it is now no longer needed

	rm dwa130_revC_drivers_linux_006.zip
	rm -r rtl8192u_linux_2.6.0006.1031.2008
fi

# un-tar the driver file and install the driver
if [ -f xbian-8192cu.tar.gz ]; then
	tar -zxf xbian-8192cu.tar.gz > /dev/null 2>&1
	install -p -m 644 8192cu.ko /lib/modules/3.2.21+/kernel/drivers/net/wireless/
	rm xbian-8192cu.tar.gz
else 
	tar -zxf 8192cu*.tar.gz > /dev/null 2>&1
	install -p -m 644 8192cu.ko /lib/modules/3.1.9+/kernel/drivers/net/wireless/
	rm 8192cu*.tar.gz
fi

# delete the driver files in the home directory - the driver has been installed so they're no longer needed.

rm 8192cu.ko
rm linux_version.txt

# now update some files to configure the driver - first /etc/network/interfaces.

# if wlan0 or another wifi is already configured in these files:
# 1 - ignore the file edits if doing a update of the currently installed driver.
# 2 - add a new device using wlanx if installing another wifi adapter
#

ADAPTER_INSTALL=N

if [ ${ADAPTER_NUMBER} != 0 ]; then
	echo
	echo "Your Pi is already configured to use a wifi adapter."
	echo
	echo "The script will allow you to add an additional wifi adapter if it uses the"
	echo "rtl8188cus driver. The wifi adapter already installed does not need to be using"
	echo "the rtl8188cus driver and may use a different driver. Use the Add option in this"
	echo "case"
	echo
	echo "The script can upgrade/re-installed the driver for the wifi adapter already"
	echo "installed if it uses the rtl8188cus driver. If the installed wifi driver uses"
	echo "the rtl8188cus driver and is not working because you have done a recent software"
	echo "update/upgrade select the Upgrade option."
	echo 
	echo 
	echo "Are you upgrading or re-installing the driver for a device already installed or"
	echo "do you want to install a new adapter that uses the rtl8188cus driver?"
	while true; 	do
		echo
		read -p "Press U if Upgrading/re-installing a driver, press A if Adding a new adapter. " -n1 ADAPTER_INSTALL
		if [ "$ADAPTER_INSTALL" != "U" ] && [ "$ADAPTER_INSTALL" != "A" ] && [ "$ADAPTER_INSTALL" != "u" ] && [ "$ADAPTER_INSTALL" != "a" ]; then
			echo " - Invalid response, enter U or A "
			echo
		else
			echo
			break
		fi
	done
fi

if [ "$ADAPTER_INSTALL" == "A" ] || [ "$ADAPTER_INSTALL" == "a" ] ||  [ "$ADAPTER_INSTALL" == "N" ]; then

	echo
	echo "modifying the /etc/network/interfaces file to add a rtl8188cus wifi adapter"
	echo
	echo "Be careful typing in the network name, SSID, and the network password, PASSWORD,"
	echo "if your network uses WEP or WPA/WPA2. If either are incorrect the wifi will not"
	echo "connect to the network and you will need to re-write the SD card and repeat the"
	echo "installation."
	echo
	echo "Is your network unsecured so does NOT need a password or is it secured and needs"
	echo "a password to connect to the wireless network."
	while true; do
		echo
		read -p "Press U if the network is unsecured, press E if WEP, or A if WPA/WPA2. " -n1 SECURITY
		if [ "$SECURITY" != "U" ] && [ "$SECURITY" != "E" ] && [ "$SECURITY" != "A" ] && [ "$SECURITY" != "u" ] && [ "$SECURITY" != "e" ] && [ "$SECURITY" != "a" ]; then
			echo " - Invalid response, enter U, E or A "
			echo
		else
			echo
			break
		fi
	done

	while true; do
		echo
		read -p "Please enter the Network SSID - " SSID
		echo
		echo "Your network SSID is \"$SSID\", is that correct?"
		read -p "press Y to continue, any other key to re-enter the SSID. " -n1 RESPONSE
		if [ "$RESPONSE" == "Y" ] || [ "$RESPONSE" == "y" ]; then
			echo
			break
		fi
		echo
	done

	if [ "$SECURITY" != "U" ] && [ "$SECURITY" != "u" ]; then
		while true; do
			echo
			read -p "Please enter the Network PASSWORD - " PASSWORD
			echo
			echo "Your network PASSWORD is \"$PASSWORD\", is that correct?"
			read -p "press Y to continue, any other key to re-enter the PASSWORD." -n1 RESPONSE
			if [ "$RESPONSE" == "Y" ] || [ "$RESPONSE" == "y" ]; then
				echo
				break
			fi
			echo
		done
	fi

# add line "allow-hotplug wlan0" to file /etc/network/interfaces

	echo >> /etc/network/interfaces
	echo "allow-hotplug wlan$ADAPTER_NUMBER" >> /etc/network/interfaces

# add line "auto wlan0" to file /etc/network/interfaces

	echo >> /etc/network/interfaces
	echo "auto wlan$ADAPTER_NUMBER" >> /etc/network/interfaces

# add line "iface wlan0 inet dhcp" to file /etc/network/interfaces

	echo >> /etc/network/interfaces
	echo "iface wlan$ADAPTER_NUMBER inet dhcp" >> /etc/network/interfaces

# if unsecured or using WEP add line "wireless-essid $SSID" to file /etc/network/interfaces
# if using WPA/WPA add line "wpa-ssid \"$SSID\"" to file /etc/network/interfaces

	if [ "$SECURITY" != "A" ] && [ "$SECURITY" != "a" ]; then
		echo "wireless-essid $SSID" >> /etc/network/interfaces
	else
		echo "wpa-ssid \"$SSID\"" >> /etc/network/interfaces
	fi

# if using WEP add line "wireless-key $PASSWORD" to file /etc/network/interfaces

	if [ "$SECURITY" = "E" ] || [ "$SECURITY" = "e" ]; then
		echo "wireless-key $PASSWORD" >> /etc/network/interfaces
	fi

# if using WPA/WPA2 add line "wpa-psk \"$PASSWORD\"" to file /etc/network/interfaces

	if [ "$SECURITY" = "A" ] || [ "$SECURITY" = "a" ]; then
		echo "wpa-psk \"$PASSWORD\"" >> /etc/network/interfaces
	fi

# now update module blacklist.conf file to disable any old rtl8192cu driver file

	if ! grep -q "blacklist rtl8192cu" /etc/modprobe.d/blacklist.conf 2> /dev/null ; then

		echo
		echo "modifying /etc/modprobe.d/blacklist.conf to blacklist the old rtl8192cu driver"
		echo
		sleep 2

# add line "blacklist rtl8192cu" to file /etc/modprobe.d/blacklist.conf

		echo >> /etc/modprobe.d/blacklist.conf
		echo "blacklist rtl8192cu" >> /etc/modprobe.d/blacklist.conf
	fi

# now update the /etc/module file to add the new driver module name

	if ! grep -q "8192cu" /etc/modules 2> /dev/null ; then

		echo
		echo "modifying the /etc/modules file to add the new 8192cu driver"
		echo
		sleep 2

# add line "8192cu" to file /etc/modules

		echo >> /etc/modules
		echo "8192cu" >> /etc/modules
	fi
fi

# now update the module dependencies

echo
echo "updating system module dependencies - I know, I don't know what this is as well"
echo

depmod -a

# check if we are already using a wifi adapter when we are adding a new one 

let ADAPTER_COUNTER=ADAPTER_NUMBER
if [ "$ADAPTER_INSTALL" == "A" ] || [ "$ADAPTER_INSTALL" == "a" ] ; then
	ADAPTER_COUNTER=0
	while [ ${ADAPTER_COUNTER} != ${ADAPTER_NUMBER} ]; do
		ifconfig wlan$ADAPTER_COUNTER > temp.tmp 2>/dev/null
		if grep -q "wlan$ADAPTER_COUNTER" temp.tmp ; then
			ifdown wlan0 2> /dev/null
			echo
			echo "Unplug the wifi adapter you are using and plug in the new adapter. It should"
			echo "start automatically. Just wait a while for the wifi adpter LED to start flashing"
			read -p "then press any key to continue..." -n1 -s
			echo
			echo
			break
		else
			let ADAPTER_COUNTER=ADAPTER_COUNTER+1
		fi
	done
fi
if [ ${ADAPTER_COUNTER} == ${ADAPTER_NUMBER} ]; then
	echo
	echo "Plug in the wifi adapter. It should start automatically. Just wait a while for"
	read -p "the wifi adapter LED to start flashing then press any key to continue..." -n1 -s
	echo
	echo
fi

#
# check if the wifi has started.
#

ADAPTER_COUNTER=0

while true; do
	if grep -q -x "iface wlan$ADAPTER_COUNTER inet dhcp" /etc/network/interfaces ; then
		let ADAPTER_COUNTER=ADAPTER_COUNTER+1
	else
		if [ ${ADAPTER_COUNTER} == 1 ]; then
			echo "You now have $ADAPTER_COUNTER wifi adapter configured"
		else
			echo "You now have $ADAPTER_COUNTER wifi adapters configured"
		fi
	echo
	break
	fi
done

# Look for a wifi adapter to come ready

EXITSTATUS=1
until [ ${EXITSTATUS} == 0 ]; do
	ADAPTER_NUMBER=0
	while [ ${ADAPTER_NUMBER} != ${ADAPTER_COUNTER} ]; do
		ifconfig wlan$ADAPTER_NUMBER > temp.tmp 2>/dev/null
		if grep -q "wlan$ADAPTER_NUMBER" temp.tmp ; then
			EXITSTATUS=$?
			echo "The wifi adapter driver is installed. Waiting for the wifi adapter to connect."
			break
		else
			let ADAPTER_NUMBER=ADAPTER_NUMBER+1
		fi
	done
done

sleep 5

EXITSTATUS=1
COUNTER=0
until [ ${EXITSTATUS} == 0 ]; do
	ifconfig wlan$ADAPTER_NUMBER > temp.tmp 2>/dev/null
	grep -q "inet addr:" temp.tmp
	EXITSTATUS=$?
	if [ ${EXITSTATUS} != 0 ]; then
		if [ ${COUNTER} == 0 ]; then
			ifup --force wlan$ADAPTER_NUMBER  >/dev/null 2>&1
		fi
		if [ ${COUNTER} == 4 ]; then
			COUNTER=0
		fi
	let COUNTER=COUNTER+1
	sleep 5
	fi
rm temp.tmp
done

echo
echo "The wifi adapter wlan$ADAPTER_NUMBER is now connected."
echo
echo "Check the wlan$ADAPTER_NUMBER settings. This will show the network IP address assigned to the"
echo "wifi adapter and other parameters for the wifi adapter."
echo

ifconfig wlan$ADAPTER_NUMBER

uname -a > linux_version.txt
if grep -q "Linux raspberry-pi 3.2.21+" linux_version.txt ; then
	rm linux_version.txt
	echo
	echo "The basic wifi driver is now loaded and operating. The script will now terminate"
	echo
	echo "Have fun with your Raspberry Pi."
	echo
	exit
fi
rm linux_version.txt
echo
echo "The basic wifi driver is now loaded and operating. You may now terminate the"
echo "installation if you want to, however, your firmware and software may not be up to"
echo "date. If you decide to terminate the installation but then later update the"
echo "firmware and software the wifi may stop working and you will need to upgrade the"
echo "wifi driver."
echo
read -p "Press cntl-C to terminate the installation, any other key to continue..." -n1 -s
echo

# now we have an internet connection the Pi can grab a few updates. first update the list
# of available packages to bring it upto date

# update sources list if debian6-19-04-2012 release

uname -a > linux_version.txt
if grep -q "Linux raspberrypi 3.1.9+ #90 Wed Apr 18 18:23:05 BST 2012 armv6l GNU/Linux" linux_version.txt ; then
	if grep -q "deb http://ftp.uk.debian.org/debian/ squeeze main non-free" /etc/apt/sources.list ; then

		echo
		echo "Updating the apt-get sources.list file. There is an issue with the sources.list"
		echo "file which may generate an error when using apt-get to install/update software."
		echo
		sleep 3

		echo 'deb http://ftp.uk.debian.org/debian/ squeeze main contrib non-free' > /etc/apt/sources.list
		echo >> /etc/apt/sources.list
		echo >> /etc/apt/sources.list
		echo '# Nokia Qt5 development' >> /etc/apt/sources.list
		echo 'deb http://archive.qmh-project.org/rpi/debian/ unstable main' >> /etc/apt/sources.list
		echo >> /etc/apt/sources.list
	fi
fi


echo
echo "Updating the Debian sofware packages list to bring it up to date."
echo

EXITSTATUS=-1
until [ ${EXITSTATUS} == 0 ]; do
	apt-get update
	EXITSTATUS=$?
	if [ ${EXITSTATUS} != 0 ]; then
		sleep 4
	fi
done

echo
echo "Upgrading the loaded Debian software packages to the latest version."
echo

EXITSTATUS=-1
until [ ${EXITSTATUS} == 0 ]; do
	apt-get -y --force-yes upgrade
	EXITSTATUS=$?
	if [ ${EXITSTATUS} != 0 ]; then
		sleep 4
	fi
done

# If Raspbian Hexxeh get ntp and fake-hwclock packages to set the time before running rpi-update

if grep -q "#52 Tue May 8 23:49:32 BST 2012" linux_version.txt ; then
	EXITSTATUS=-1
	until [ ${EXITSTATUS} == 0 ]; do
		apt-get -y install ntp fake-hwclock
		EXITSTATUS=$?
		if [ ${EXITSTATUS} != 0 ]; then
			sleep 4
		fi
	done
fi

rm linux_version.txt

# now update dhcp. the current dhcp may only allow you to access the Pi using it's ip address.
# the update should allow accessing the Pi using it's host name.

EXITSTATUS=-1
until [ ${EXITSTATUS} == 0 ]; do
	apt-get install -y isc-dhcp-client
	EXITSTATUS=$?
	if [ ${EXITSTATUS} != 0 ]; then
		sleep 4
	fi
done


if [ ! -f /usr/bin/rpi-update ]; then

	echo
	echo
	echo "rpi-update is not installed. Installing rpi-update - automatic rpi software and"
	echo "firmware updater."
	echo
	sleep 1

	EXITSTATUS=-1
	until [ ${EXITSTATUS} == 0 ]; do
		apt-get -y install git-core ca-certificates binutils
		EXITSTATUS=$?
		if [ ${EXITSTATUS} != 0 ]; then
			sleep 4
		fi
	done

	EXITSTATUS=-1
	until [ ${EXITSTATUS} == 0 ]; do
		wget http://goo.gl/1BOfJ -O /usr/bin/rpi-update && chmod +x /usr/bin/rpi-update 2> /dev/null
		EXITSTATUS=$?
		if [ ${EXITSTATUS} != 0 ]; then
			sleep 4
		fi
	done
else
	echo
	echo "rpi-update is already installed."
fi

echo
echo "Downloading the latest wifi driver. This will be installed after rpi-update"
echo "has run."
echo

EXITSTATUS=-1
until [ ${EXITSTATUS} == 0 ]; do
	wget http://dl.dropbox.com/u/80256631/8192cu-latest.tar.gz 2> /dev/null
	EXITSTATUS=$?
	if [ ${EXITSTATUS} != 0 ]; then
		sleep 4
	fi
done

tar -zxf 8192cu-latest.tar.gz > /dev/null 2>&1
rm 8192cu-latest.tar.gz

# compare latest and currently installed driver. If they are the same don't
# run rpi-update. Running rpi-update may load a new firmware version that is
# incompatible with the latest available driver

if cmp 8192cu.ko /lib/modules/3.1.9+/kernel/drivers/net/wireless/8192cu.ko > /dev/null ; then

# The currently loaded driver is the latest available. Finish without running rpi-update

	echo "The currently installed wifi driver is the latest available. To avoid the"
	echo "possibility that problems could occur if rpi-update is run and it loads a new"
	echo "kernel version that is incompatible with the currently installed driver"
	echo "rpi-update will not run this time around."
	echo
	echo "The script has now finished and will terminate."

else

	echo
	echo "rpi-update will now run to update the Pi's firmware and software"
	echo
	sleep 1

	rpi-update
	EXITSTATUS=$?

# running rpi-update from the command line will allow you to load any new updates when the become available
# so don't forget to run it occasionally especially if you think you may have a software issue.

# install the new driver software and update the module dependencies

	if [ ${EXITSTATUS} == 0 ]; then
		echo
		echo "Installing the new wifi driver. "
		echo

		install -p -m 644 8192cu.ko /lib/modules/3.1.9+/kernel/drivers/net/wireless/
		depmod -a

		rm 8192cu.ko

		echo
		echo "Plug in the wifi adapter if not already plugged in and unplug the wired"
		echo "connection. The wifi should start when the RPi restarts."
		echo
		echo "It is possible the RPi may not reboot correctly but get stuck trying to reboot."
		echo "If it doesn't reboot correctly turn of the power to the RPi, wait for all LEDS "
		echo "to turn off, wait a few seconds and then turn the power back on. It should boot"
		echo "up and the wireless should connect. I have never seen it fail to boot after "
		echo "switching the power off and then on again after failing to reboot correctly "
		echo "while testing this script."
		echo
		read -p "Press any key to continue..." -n1 -s
		echo

# remove the firmware and firmware directory for the old driver - they are no longer needed

		rm -r /usr/local/lib/firmware/RTL8192U > /dev/null 2>&1
		rm /boot/*.deb > /dev/null 2>&1
#	rm /boot/install-rtl8188cus-latest.sh > /dev/null 2>&1
		reboot

	else

		echo
		echo "rpi-update returned an error"
		echo
		echo "As the latest version of the driver expects rpi-update to have run the driver"
		echo "has not been installed - the script will now terminate."
		echo

	fi
fi

# time to finish!

echo
echo "Have fun with your Raspberry Pi."
echo
