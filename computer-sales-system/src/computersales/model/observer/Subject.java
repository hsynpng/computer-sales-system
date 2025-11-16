package computersales.model.observer;

public interface Subject {
    void attach(Observer observer);
    void detach(Observer observer);
    void notifyObservers();
}


// Gözlemlenen nesnelerin temel arayüzü (gözlemcileri ekle/sil/haberdar et)