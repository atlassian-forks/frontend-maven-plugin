def p = "sh $basedir/install-fnm.sh".execute()
p.waitFor()
println p.text