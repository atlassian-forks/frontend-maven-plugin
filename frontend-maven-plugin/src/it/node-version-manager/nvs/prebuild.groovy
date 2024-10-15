def p = "bash $basedir/install-nvs.sh".execute()
p.waitFor()
println p.text