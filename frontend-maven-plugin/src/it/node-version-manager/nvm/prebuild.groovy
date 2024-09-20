def p = "bash $basedir/install-nvm.sh".execute()
p.waitFor()
println p.text