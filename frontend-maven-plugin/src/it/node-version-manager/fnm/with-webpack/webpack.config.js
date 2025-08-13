const { version } = require('process');
console.info(`node version used:${version}`)

// webpack.config.js is a trigger file for webpack mojo
module.exports = {
    mode: 'production'
};
