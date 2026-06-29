# feign启动扫描器

通过spring boot 启动时使用`EnableJvsFeignClients`扫描器,将扫描cn.bctools.**.api包下所有的api
服务请求时,必须以`R`对象返回,并在请求时,默认携带请求版本号,无需开发者再去操作请求版本,返回数据约定一致,如果出现业务异常,将自动抛出异常信息,将此异常信息以R返回给业务数据,失败将有message形式返回,正常则直接返回R.ok(data)
