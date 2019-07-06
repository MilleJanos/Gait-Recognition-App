package ms.sapientia.ro.gaitrecognitionapp.model;

/**
 * This interface is to create simple function pointer for asynchronous methods, which
 * calls the Do() method after his job is over.
 */
public interface IAfter {

    void Do();

}
