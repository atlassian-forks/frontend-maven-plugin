def p = "sh $basedir/install-nvs.sh".execute()
p.waitFor()
println p.text