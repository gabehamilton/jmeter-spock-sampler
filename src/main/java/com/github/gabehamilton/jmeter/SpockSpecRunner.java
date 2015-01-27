package com.github.gabehamilton.jmeter;

import junit.framework.Protectable;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.spockframework.runtime.Sputnik;

/**
 * Created by gabe on 1/23/15.
 * inspired by org.jboss.arquillian.spock.container.SpockTestRunner
 */
public class SpockSpecRunner {

    public static Result execute(final Class<?> classOfspecToRun) throws InitializationError {
            //, final String methodName

        final Result testResult = new Result();

        final Sputnik spockRunner = new Sputnik(classOfspecToRun);
//      spockRunner.filter(new SpockSpecificationFilter(spockRunner, methodName));
        runTest(spockRunner, testResult);

        return testResult;
    }

    public static void runTest(final Sputnik spockRunner, final Result testResult) {
        final RunNotifier notifier = new RunNotifier();
        notifier.addFirstListener(testResult.createListener());

//        for (RunListener listener : getRunListeners())
//        {
//            notifier.addListener(listener);
//        }

        spockRunner.run(notifier);
    }

}
