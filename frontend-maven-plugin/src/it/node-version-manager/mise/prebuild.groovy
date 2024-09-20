def p = "bash $basedir/install-mise.sh".execute()
p.waitFor()
println p.text