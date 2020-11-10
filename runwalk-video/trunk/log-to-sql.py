#!/usr/bin/python3
import re
import codecs
import functools

filepath = 'log.txt'
with codecs.open(filepath, 'r', encoding='utf-8', errors = 'ignore') as fp:
    line = fp.readline()
    insertline = ""
    while line:
        insert = "INSERT INTO" in line
        update = "UPDATE" in line
        bind = "bind" in line
        if insert:
            insertline = line
            #insertline = re.sub("VALUES.*", "", insertline)
            insertline = re.sub(".*INSERT INTO", "INSERT INTO", insertline)
        if update:
            insertline = line
            insertline = re.sub(".*UPDATE", "UPDATE", insertline)
        if bind and insertline != "":
            m = re.search("\[(.*)\]", line)
            if m:
                values = m.group(1).split(", ")
                func = lambda line, value : line.replace("?", '\'%s\'' % value if " " in value else value, 1)
                line = functools.reduce(func, values, insertline)
                print(line + ';')
        if not insert and not update:
            insertline = ""
        line = fp.readline()
