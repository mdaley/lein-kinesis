# lein-kinesis

A Leiningen plugin to run an in-memory instance of Amazon's kinesis using Michael Hart's [kinesalite](https://github.com/mhart/kinesalite).

## Usage

Add `[lein-kinesis "0.1.6"]` to the `:plugins` vector of your project.

Start the kinesis service when running lein by specifying it before the other tasks that you are running, for example:

    $ lein kinesis run

Once the task completes, the kinesalite service will also be terminated.

If for some reason you'd like to run the plugin by itself you can invoke it like this, with any further tasks:

    $ lein kinesis
    
When you want to stop it just press <kbd>Ctrl</kbd>+<kbd>C</kbd>.
   
## Configuration

There is only one configuration option:

```clojure
(defproject my-project "1.0.0-SNAPSHOT"
  ...
  :plugins [[lein-kinesis "0.1.0"]]
  ...
  :kinesis {:port 12345 ; optional - port on which the service listens, default value is 8083
            :ssl true; optional - triggers the use of the --ssl flag, default is false (ie no --ssl flag)
            }
  ...
)
```

## License

Copyright © 2015 Matthew Daley

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
