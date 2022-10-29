
#!/bin/sh
#
# Copyright 2004-2018 Crypto-Pro. All rights reserved.
#
# This is proprietary information of
# Crypto-Pro company.
#
# Any part of this file can not be copied, 
# corrected, translated into other languages,
# localized or modified by any means,
# compiled, transferred over a network from or to
# any computer system without preliminary
# agreement with Crypto-Pro company
#

if [ $(id -u) != 0 ]; then
  echo "Root only accessed"
  exit 1   
  # need elevate script privileges
fi

BASE_DIR=/var/opt/cprocsp
echo "Creating keys directory..."

mkdir -p $BASE_DIR/keys
chmod a+rwx $BASE_DIR/keys

echo "Complete."
echo "Creating tmp directory..."

mkdir -p $BASE_DIR/tmp
chmod a+rwx $BASE_DIR/tmp

echo "Complete."
