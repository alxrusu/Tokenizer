import os
import sys

f=open(sys.argv[1],"rb")
words=f.readlines()
f.close()
wrds=list()
for w in words:
	wrds.append(w.strip())
print wrds