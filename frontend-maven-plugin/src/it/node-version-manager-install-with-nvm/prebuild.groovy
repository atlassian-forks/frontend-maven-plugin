def p = "sh $basedir/install-nvm.sh".execute()
p.waitFor()
println p.text