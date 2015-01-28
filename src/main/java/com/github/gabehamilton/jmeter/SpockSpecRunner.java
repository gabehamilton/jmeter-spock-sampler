package com.github.gabehamilton.jmeter;

import org.junit.runner.Result;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.spockframework.runtime.Sputnik;
import org.jboss.arquillian.spock.container.SpockSpecificationFilter;

import java.util.Collections;
import java.util.List;

/**
 * Created by gabe on 1/23/15.
 * derived from org.jboss.arquillian.spock.container.SpockTestRunner
 */
public class SpockSpecRunner {

    public static Result execute(final Class<?> classOfspecToRun) throws InitializationError, NoTestsRemainException {
        return SpockSpecRunner.execute(classOfspecToRun, null);
    }
    public static Result execute(final Class<?> classOfspecToRun, final String methodName) throws InitializationError, NoTestsRemainException {

        final Result testResult = new Result();

        Sputnik spockRunner = new Sputnik(classOfspecToRun);
        if(methodName != null && !methodName.equals("")) {
            SpockSpecificationFilter filter = new SpockSpecificationFilter(spockRunner, methodName);
            spockRunner.filter(filter);
        }

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

    /**
     * Overwrite to provide additional run listeners.
     */
    protected List<RunListener> getRunListeners()
    {
        return Collections.emptyList();
    }

}
