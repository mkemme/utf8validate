#!/bin/bash
# Martins Kemme
# This script checks encoding of all SQL, PLS, PLB, LDT and WFT files
# in current directory and subdirectories

find . -iname "*.sql" -or -iname "*.pls" -or -iname "*.plb" -or -iname "*.ldt" -or -iname "*.wft" | while read FILENAME
do
	java -jar Utf8Validate.jar < "${FILENAME}" > /dev/null
	EXITCODE=$?
	case ${EXITCODE} in
		0) echo "          UTF-8: ${FILENAME}";;
		1) echo " UTF-8 with BOM: ${FILENAME}";;
		2) echo "    Plain ASCII: ${FILENAME}";;
		*) echo "Not valid UTF-8: ${FILENAME}";;
	esac
done