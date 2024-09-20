def p = "bash $basedir/install-asdf.sh".execute()
p.waitFor()
println p.text