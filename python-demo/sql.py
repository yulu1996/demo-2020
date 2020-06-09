f = open(r'C:\Users\YULU\Desktop\新建文件夹 (5)\表定义SQL\用友表结构\HI_STAPPLY.sql', 'r')
e = f.readline()
print(e)
tablename = e.split('.')[-1][1:-3]
print(tablename)
w = open(r'C:\Users\YULU\Desktop\新建文件夹 (5)\gpkafka\ncc_hi_stapply.yaml', 'w')
w.write('DATABASE: dhc\nUSER: gpadmin\nPASSWORD: rd123456\nHOST: mdw\nPORT: 5432\nVERSION: 2\n')
w.write('KAFKA:\n')
w.write('   INPUT:\n')
w.write('      SOURCE:\n')
w.write('        BROKERS: 172.16.33.40:9092\n')
w.write('        TOPIC: ' + tablename + '\n')
w.write('      VALUE:\n')
w.write('        COLUMNS:\n')
w.write('          - NAME: c1\n')
w.write('            TYPE: json\n')
w.write('        FORMAT: json\n')
w.write('      ERROR_LIMIT: 100\n')
w.write('   OUTPUT:\n')
w.write('      MODE: MERGE\n')
w.write('      MATCH_COLUMNS:\n')
w.write('        - staff_code\n')
w.write('      UPDATE_COLUMNS:\n')

line = f.readline()
while ',' in line:
    print(line.split('\"')[1])
    w.write('        - ' + line.split('\"')[1] + '\n')
    # print(line.split('\"')[2].split(' ')[1].split('(')[0])
    line = f.readline()

w.write('      ORDER_COLUMNS:\n')
w.write('        - current_ts\n')
w.write('      DELETE_CONDITION: c1->>\'op_type\' = \'D\'\n')
w.write('      SCHEMA: NCC_10\n')
w.write('      TABLE: ' + tablename + '\n')
w.write('      MAPPING:\n')
f.seek(0)
f.readline()
line = f.readline()
while ',' in line:
    name = line.split('\"')[1]
    type = line.split('\"')[2].split(' ')[1].split('(')[0]
    print(type)
    if type == 'VARCHAR2':
        typename = 'text'
    elif type == 'VARCHAR':
        typename = 'text'
    elif type == 'CHAR':
        typename = 'text'
    elif type == 'BLOB,':
        typename = 'text'
    elif type == 'NUMBER':
        typename = 'numeric'
    else:
        typename = '***********************'

    w.write('         - NAME: ' + name + '\n')
    w.write('           EXPRESSION: |\n')
    w.write(
        '              CASE WHEN ((c1->\'after\')::json is not null) THEN (c1->\'after\'->>\'' + name + '\')::' + typename + '\n')
    w.write('              ELSE (c1->\'before\'->>\'' + name + '\')::' + typename + ' end\n')
    line = f.readline()

w.write('         - NAME: current_ts\n')
w.write('           EXPRESSION: (c1->>\'current_ts\')::timestamp\n')
w.write('   COMMIT:\n')
w.write('      MINIMAL_INTERVAL: 2000')
f.close()
w.close()
