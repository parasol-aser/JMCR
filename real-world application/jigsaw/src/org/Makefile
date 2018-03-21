PACKAGE="org"

JIGSAWPACKAGES = \
	w3c

PACKAGES = $(JIGSAWPACKAGES)

TOP=../..
include $(TOP)/makefiles/package.make

jigsawdoc::
	rm -rf ../../../Doc/Programmer/api/*
	javadoc -J-ms32m -J-mx64m -d ../../../Doc/Programmer/api/ \
		`make -s dumpjigsawpackages`
