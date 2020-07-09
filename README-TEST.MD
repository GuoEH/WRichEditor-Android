/////////------------- quote 格式测试： ------------

1.  A   B   C | D   E   F                       带格式通过
2.  A   B   C | D | E   F                       带格式通过
3. |A   B   C   D   E   F                       带格式通过

4.  A   B   C   D   E   F  '/n' |               带格式通过
5.  A   B   C   D | E   F  '/n'                 带格式通过
6.  A   B   C   D   E   F |'/n'                 带格式通过

7.  A   B   C   D   E | F   G   '/n'            带格式通过
    H   I   J   K   L | M   N   ('/n')               
    
8.  A   B   C   D   E | F   G   '/n'            带格式通过
    H   I   J   K   L   M   N   ('/n')    
    
9.  A   B   C | D   E | F   G   '/n'            带格式通过
    H   I   J   K   L   M   N   ('/n')    