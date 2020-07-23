# greenplum 

## 管理Greenplum数据库访问
##### 配置客户端认证
1. 使用文本编辑器，打开$MASTER_DATA_DIRECTORY/pg_hba.conf 文件。
2. `host   database   role   address   authentication-method`
3. 保存并关闭文件。
4. 重新加载pg_hba.conf配置文件以使更改生效：`$ gpstop -u`
##### 更改允许的连接数
1. `source /usr/local/greenplum-db/greenplum_path.sh`
2. 设置max_connections参数取值。此gpconfig命令将segment实例节点上的参数值设置为1000，将master节点上的参数值设置为200。  
`$ gpconfig -c max_connections -v 1000 -m 200`  
该参数值segement节点上的必须大于master节点上的。推荐segment节点的max_connections参数值是master节点的5-10倍。
3. 设置max_prepared_transactions参数取值。此gpconfig命令将master节点和segment实例节点上的取值都这会为200.  
`$ gpconfig -c max_prepared_transactions -v 200`  
segment节点的max_prepared_transactions参数值必须大于等于master节点的max_connections参数值。  
4. 停止并重启Greenplum数据库系统。  
`$ gpstop -r`
5. 可以使用gpconfig -s选项，检查master和segment节点的参数值。此gpconfig命令显示max_connections参数的取值。  
`$ gpconfig -s max_connections`
## 配置和管理流服务器
Greenplum流服务器（GPSS）管理客户端（例如，Pivotal Greenplum-Informatica连接器）与Greenplum数据库之间的通信和数据传输。在使用服务将数据加载到Greenplum数据库之前，必须配置并启动GPSS实例。  
##### 注册GPSS扩展
Greenplum数据库和Greenplum Streaming Server下载软件包将安装GPSS扩展。此扩展名必须在Greenplum用户使用GPSS将数据写入Greenplum表的每个数据库中注册。  

当Greenplum超级用户或数据库所有者首次启动加载作业时，GPSS会自动在数据库中注册其扩展名。如果非特权Greenplum用户将是该数据库中GPSS的第一个或唯一用户，则必须在数据库中手动注册该扩展名。  

以Greenplum数据库超级用户或数据库所有者的身份执行以下过程，以手动注册GPSS扩展名：
1. 打开一个新的终端窗口，以 gpadmin管理用户，并设置Greenplum环境。例如：
    ```$xslt
    $ ssh gpadmin@mdw
    gpadmin@mdw $./usr/local/greenplum-db/greenplum_path.sh
    ```
2. 开始 psql子系统，连接到要在其中注册GPSS格式化程序功能的数据库。例如：
    ```$xslt
    $ psql -d testdb
    ```
3. 输入以下命令以注册扩展名：
    ```$xslt
    testdb=# CREATE EXTENSION gpss;
    ```
4.Perform steps 2 and 3 for each database in which the Greenplum Streaming Server will write client data.
##### 配置Greenplum流服务器
您可以通过JSON格式的配置文件来配置Greenplum Streaming Server的调用。此配置文件包含一些属性，这些属性标识GPSS服务的侦听地址以及gpfdist 服务主机，绑定地址和端口号。您可以在文件中指定加密选项，可以配置密码阴影编码/解码密钥，还可以配置GPSS是否重用外部表。  
名为GPSS JSON的示例配置文件的内容 gpsscfg1.json:
```$xslt
{
    "ListenAddress": {
        "Host": "",
        "Port": 5019
    },
    "Gpfdist": {
        "Host": "",
        "Port": 8319
    }
}
```
##### 运行Greenplum流服务器
您使用 gpss实用程序，以在本地主机上启动Greenplum Streaming Server的实例。运行命令时，请提供配置文件的名称，该文件定义GPSS和 gpfdist服务实例。您还可以指定目录名称，gpss写入日志文件。例如，要启动一个GPSS实例，指定一个名为的日志目录 gpsslogs 相对于当前工作目录：
```$xslt
$ gpss gpsscfg1.json --log-dir ./gpsslogs
```
gpss的默认操作模式是等待，然后使用来自客户端的作业请求和数据。在此模式下运行时，gpss无限期地等待。您可以使用Control-c中断并退出命令。您也可以选择运行gpss 在后台（&）。在这两种情况下 gpss 将日志和状态消息写入 标准输出。  
```$xslt
注意： gpss跟踪内存中客户端作业的加载进度。停止GPSS服务器实例时，您将丢失所有已注册的作业。重新启动GPSS实例后，您必须重新提交所需的所有先前提交的作业。gpss 从上次加载偏移量开始恢复作业。
```
##### 管理GPSS日志文件
如果您指定 -l 要么 --log-dir 开始时的选项 gpss 或运行 gpsscli子命令，GPSS将日志消息写入您指定目录中的文件。如果您不提供此选项，则GPSS会将日志消息写入文件中的$HOME/gpAdminLogs 目录。  
GPSS将服务器日志消息写入具有以下命名格式的文件，其中 date标识创建日志文件的日期。该日期反映了您开始gpss服务器实例，或该服务器实例的日志的日期（请参见下面的 旋转GPSS服务器日志文件）：
```$xslt
gpss_date.log
```
GPSS创建日志文件后，它将附加所有写入的服务器和客户端日志消息 日期 到各自的文件。
##### 旋转GPSS服务器日志文件
如果日志文件为 gpss 服务器实例变得太大，您可以选择存档当前日志，并从一个空的日志文件重新开始。  
要旋转GPSS服务器日志文件，您必须：
1. 重命名现有的日志文件。例如：  
    ```
    gpadmin@gpmaster$ mv logdir/gpss_date.log logdir/gpss_date.log.1
    ```
2. 发送 SIGUSR2 发信号给 gpss 服务器进程。您可以通过运行以下命令获取GPSS实例的进程IDps命令。例如：
    ```$xslt
    gpadmin@gpmaster$ ps -ef | grep gpss
    gpadmin@gpmaster$ kill -SIGUSR2 gpss_pid
    ```
   
    > 注意：可能有多个gpss服务器进程在系统上运行。确保将信号发送到所需的过程。

    当GPSS接收到该信号时，它会发出一条日志消息，该消息标识了重置日志文件的时间。例如:  
    `... -[INFO]:-Set gpss log file rotate at 20190911:20:59:36.093`
##### 使用gpsscli客户端
| 指令 | 描述 |
| :----- | :---- |
| help | 显示命令帮助 |
| history | 显示工作记录（已弃用） |
| list | 列出工作及其状态 |
| load | 运行单命令加载 |
| progress | 显示工作进度 |
| remove | 删除工作 |
| start | 开始工作 |
| status | 显示工作状态 |
| stop | 停止工作 |
| submit | 提交工作 |
| wait | 等待工作停止 |
所有子命令都包含选项，这些选项使您可以指定要为请求提供服务的GPSS实例的主机和/或端口号（--config或--gpss-host和--gpss-port）。您还可以指定GPSS写入的目录gpsscli 日志文件 （--log-dir）。
示例：
```$xslt
$ gpsscli submit --name nightly_order_upload loadcfg.yaml
$ gpsscli start nightly_order_upload
$ gpsscli list --all
$ gpsscli status nightly_order_upload
$ gpsscli progress nightly_order_upload
$ gpsscli wait	nightly_order_upload
$ gpsscli stop	nightly_order_upload
$ gpsscli remove nightly_order_upload
```
##### gpkafka配置文件
```yaml
DATABASE: db_name
USER: user_name
PASSWORD: password
HOST: host
PORT: greenplum_port
VERSION: 2
KAFKA:
   INPUT:
      SOURCE:
        BROKERS: kafka_broker_host:broker_port [, ... ]
        TOPIC: kafka_topic
      [VALUE:
        COLUMNS:
           - NAME: { column_name | __IGNORED__ }
             TYPE: column_data_type
           [ ... ]
         FORMAT: value_data_format
         [[DELIMITED_OPTION:
            DELIMITER: delimiter_string] |
         [AVRO_OPTION:
            SCHEMA_REGISTRY_ADDR: http://schemareg_host:schemareg_port [, ... ]]] |
         [CUSTOM_OPTION:
            NAME: udf_name
            PARAMSTR: udf_parameter_string]]
      [KEY:
        COLUMNS:
           - NAME: { column_name | __IGNORED__ }
             TYPE: column_data_type
           [ ... ]
         FORMAT: key_data_format
         [[DELIMITED_OPTION:
            DELIMITER: delimiter_string] |
         [AVRO_OPTION:
            SCHEMA_REGISTRY_ADDR: http://schemareg_host:schemareg_port [, ... ]] |
         [CUSTOM_OPTION:
            NAME: udf_name
            PARAMSTR: udf_parameter_string]]
      [FILTER: filter_string]
      ERROR_LIMIT: { num_errors | percentage_errors }
   OUTPUT:
      [SCHEMA: output_schema_name]
      TABLE: table_name
      [MODE: mode]
      [MATCH_COLUMNS: 
         - match_column_name
         [ ... ]]
      [ORDER_COLUMNS: 
         - order_column_name
         [ ... ]]
      [UPDATE_COLUMNS: 
         - update_column_name
         [ ... ]]
      [UPDATE_CONDITION: update_condition]
      [DELETE_CONDITION: delete_condition]
      [MAPPING: 
         - NAME: target_column_name
           EXPRESSION: { source_column_name | expression } 
         [ ... ]
           |
         target_column_name : { source_column_name | expression }
         [ ... ] ]
   [METADATA:
      [SCHEMA: metadata_schema_name]]
   COMMIT:
      MAX_ROW: num_rows
      MINIMAL_INTERVAL: wait_time
   [POLL:
      BATCHSIZE: num_records
      TIMEOUT: poll_time]
   [TASK:
      POST_BATCH_SQL: udf_or_sql_to_run
      BATCH_INTERVAL: num_batches]
   [PROPERTIES:
      kafka_property_name: kafka_property_value
      [ ... ]]
[SCHEDULE:
   RETRY_INTERVAL: retry_time
   MAX_RETRIES: num_retries]
```
实例：
```yaml
DATABASE: dhc
USER: gpadmin
PASSWORD: ******
HOST: mdw
PORT: 5432
VERSION: 2
KAFKA:
   INPUT:
      SOURCE:
        BROKERS: 172.16.33.40:9092
        TOPIC: project
      VALUE:
        COLUMNS:
          - NAME: c1
            TYPE: json
        FORMAT: json
      ERROR_LIMIT: 100
   OUTPUT:
      MODE: MERGE
      MATCH_COLUMNS:
        - project_uid
      UPDATE_COLUMNS:
        - item_examine_uid
        - code
        - division
        - type
        - name
        - sponsor_code
        - organization_uid
        - orgLevel_flg
        - state
        - change_flg
        - version
        - confirm_version
        - report_change_flg
        - entrust_status
        - attribute_flg
        - templet_type
        - relate_project_uid
        - coplevel_flg
        - account_flg
        - money_finish_flg
        - updatedby
        - updatetime
        - createdby
        - createtime
        - delflag
        - Column_1
        - Column_2
        - Column_3
        - project_approve_code
        - comment
        - deleted_flag
        - status_code
      ORDER_COLUMNS:
        - current_ts
      SCHEMA: dpms
      TABLE: project
      MAPPING:
         - NAME: project_uid
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'project_uid')::text
              ELSE (c1->'before'->>'project_uid')::text end
         - NAME: item_examine_uid
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'item_examine_uid')::text
              ELSE (c1->'before'->>'item_examine_uid')::text end
         - NAME: code
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'code')::text
              ELSE (c1->'before'->>'code')::text end
         - NAME: division
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'division')::text
              ELSE (c1->'before'->>'division')::text end
         - NAME: type
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'type')::text
              ELSE (c1->'before'->>'type')::text end
         - NAME: name
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'name')::text
              ELSE (c1->'before'->>'name')::text end
         - NAME: sponsor_code
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'sponsor_code')::text
              ELSE (c1->'before'->>'sponsor_code')::text end
         - NAME: organization_uid
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'organization_uid')::text
              ELSE (c1->'before'->>'organization_uid')::text end
         - NAME: orgLevel_flg
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'orgLevel_flg')::text
              ELSE (c1->'before'->>'orgLevel_flg')::text end
         - NAME: state
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'state')::text
              ELSE (c1->'before'->>'state')::text end
         - NAME: change_flg
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'change_flg')::text
              ELSE (c1->'before'->>'change_flg')::text end
         - NAME: version
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'version')::text
              ELSE (c1->'before'->>'version')::text end
         - NAME: confirm_version
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'confirm_version')::text
              ELSE (c1->'before'->>'confirm_version')::text end
         - NAME: report_change_flg
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'report_change_flg')::text
              ELSE (c1->'before'->>'report_change_flg')::text end
         - NAME: entrust_status
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'entrust_status')::text
              ELSE (c1->'before'->>'entrust_status')::text end
         - NAME: attribute_flg
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'attribute_flg')::text
              ELSE (c1->'before'->>'attribute_flg')::text end
         - NAME: templet_type
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'templet_type')::text
              ELSE (c1->'before'->>'templet_type')::text end
         - NAME: relate_project_uid
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'relate_project_uid')::text
              ELSE (c1->'before'->>'relate_project_uid')::text end
         - NAME: coplevel_flg
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'coplevel_flg')::text
              ELSE (c1->'before'->>'coplevel_flg')::text end
         - NAME: account_flg
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'account_flg')::text
              ELSE (c1->'before'->>'account_flg')::text end
         - NAME: money_finish_flg
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'money_finish_flg')::text
              ELSE (c1->'before'->>'money_finish_flg')::text end
         - NAME: updatedby
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'updatedby')::text
              ELSE (c1->'before'->>'updatedby')::text end
         - NAME: updatetime
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'updatetime')::text
              ELSE (c1->'before'->>'updatetime')::text end
         - NAME: createdby
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'createdby')::text
              ELSE (c1->'before'->>'createdby')::text end
         - NAME: createtime
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'createtime')::text
              ELSE (c1->'before'->>'createtime')::text end
         - NAME: delflag
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'delflag')::text
              ELSE (c1->'before'->>'delflag')::text end
         - NAME: Column_1
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'Column_1')::text
              ELSE (c1->'before'->>'Column_1')::text end
         - NAME: Column_2
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'Column_2')::text
              ELSE (c1->'before'->>'Column_2')::text end
         - NAME: Column_3
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'Column_3')::text
              ELSE (c1->'before'->>'Column_3')::text end
         - NAME: project_approve_code
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'project_approve_code')::text
              ELSE (c1->'before'->>'project_approve_code')::text end
         - NAME: comment
           EXPRESSION: |
              CASE WHEN ((c1->'after')::json is not null) THEN (c1->'after'->>'comment')::text
              ELSE (c1->'before'->>'comment')::text end
         - NAME: deleted_flag
           EXPRESSION: |
              CASE WHEN ((c1->>'op_type')::text = 'D') THEN true::bool
              ELSE false::bool end
         - NAME: status_code
           EXPRESSION: |
              CASE WHEN ((c1->>'op_type')::text = 'I') THEN '0'::text
              ELSE '1'::text end
         - NAME: current_ts
           EXPRESSION: (c1->>'current_ts')::timestamp
   COMMIT:
      MINIMAL_INTERVAL: 2000
```


----

| 命令 | 描述 |
| :----- | :---- |
| $ gpstart | 启动Greenplum数据库 |
| $ gpstop -r | 重启Greenplum数据库 |
| $ gpstop -u | 仅重新载入配置文件更改 |
| $ gpstop | 停止Greenplum数据库 |
| $ gpstop -M fast | 以快速模式停止Greenplum数据库 |
| $ psql -d gpdatabase -h master_host -p 5432 -U gpadmin | 用psql连接Greenplum数据库 |
| VACUUM | 从表中移除过期的行(标记过期行所使用的空间可以被重用) |
| VACUUM FULL | 从表中移除过期的行(把表重写为没有过期行) |

#### 窗口函数
| 函数 | 返回类型 | 完整语法 | 描述 |
| :----- | :---- | :---- | :---- |
| cume_dist() | double precision | CUME_DIST() OVER ( [PARTITION BY expr ] ORDER BY expr ) | 计算一组值中一个值的累积分布。具有相等值的行总是具有相同的累积分布值。 |
| dense_rank() | bigint | DENSE_RANK () OVER ( [PARTITION BY expr ] ORDER BY expr ) | 计算一个有序行组中一行的无跳跃排名值的排名。具有相等值的行会得到相同的排名值。 |
| first_value(expr) | same as input expr type | FIRST_VALUE( expr ) OVER ( [PARTITION BY expr ] ORDER BY expr [ROWS&#124;RANGE frame_expr ] ) | 返回一个有续值集合中的第一个值。 |
| lag(expr [,offset] [,default]) | same as input expr type | LAG( expr [, offset ] [, default ]) OVER ( [PARTITION BY expr ] ORDER BY expr ) | 在不做自连接的情况下，提供对于同一个表中多于一行的访问。给定一个查询返回的一系列行以及该游标的一个位置，LAG提供对位于该位置之前一个给定物理偏移量的行的访问。默认的offset为1。 default设置当偏移量超出窗口范围之外时要返回的值。如果没有指定default，默认值是空值。 |
| last_value(expr) | same as input expr type | LAST_VALUE(expr) OVER ( [PARTITION BY expr] ORDER BY expr [ROWS&#124;RANGE frame_expr] ) | 返回一个有序值集合中的最后一个值。 |
| lead(expr [,offset] [,default]) | same as input expr type | LEAD(expr [,offset] [,exprdefault]) OVER ( [PARTITION BY expr] ORDER BY expr ) | 在不做自连接的情况下，提供对于同一个表中多于一行的访问。给定一个查询返回的一系列行以及该游标的一个位置，lead提供对位于该位置之后一个给定物理偏移量的行的访问。如果没有指定offset，默认偏移量是1。default设置当偏移量超出窗口范围之外时要返回的值。如果没有指定default，默认值是空值。 |
| ntile(expr) | bigint | NTILE(expr) OVER ( [PARTITION BY expr] ORDER BY expr ) | 把一个有序数据集划分成一些桶（由expr)定义）并且为每一行分配一个桶号。 |
| percent_rank() | double precision | PERCENT_RANK () OVER ( [PARTITION BY expr] ORDER BY expr ) | 计算一个假设行R的排名减1，然后除以被计算的行数（在一个窗口分区内）减1。 |
| rank() | bigint | RANK () OVER ( [PARTITION BY expr] ORDER BY expr ) | 计算一行在一个有序值组中的排名。根据排名标准有相等值的行得到相同的排名。被占用的行数被加到排名数上来计算下一个排名值。在这种情况下，排名可能不是连续的数字。 |
| row_number() | bigint | ROW_NUMBER () OVER ( [PARTITION BY expr] ORDER BY expr ) | 为窗口分区中的每一行或者查询中的每一行分配一个唯一的编号。 |

#### 管理工具
##### analyzedb
一个提供对表的递增和并发ANALYZE操作的工具。  
概要
```
analyzedb -d dbname
        { -s schema  | 
        { -t schema.table 
        [ -i col1[, col2, ...] | 
        -x col1[, col2, ...] ] } |
        { -f | --file} config-file }
        [ -l | --list ]
        [ --gen_profile_only ]   
        [ -p parallel-level ]
        [ --full ]
        [ -v | --verbose ]
        [ --debug ]
        [ -a ]
        
        analyzedb { --clean_last | --clean_all }
        analyzedb --version
        analyzedb { -? | -h | --help }
```
##### gpbackup
创建一个可以给gprestore工具使用的Greenplum数据库备份。  
概要
```
gpbackup --dbname database_name
   [--backup-dir directory]
   [--compression-level level]
   [--data-only]
   [--debug]
   [--exclude-schema schema_name]
   [--exclude-table schema.table]
   [--exclude-table-file file_name]
   [--include-schema schema_name]
   [--include-table schema.table]
   [--include-table-file file_name]
   [--incremental [--from-timestamp backup-timestamp]]
   [--jobs int]
   [--leaf-partition-data]
   [--metadata-only]
   [--no-compression]
   [--plugin-config config_file_location]
   [--quiet]
   [--single-data-file]
   [--verbose]
   [--version]
   [--with-stats]

gpbackup --help 
```