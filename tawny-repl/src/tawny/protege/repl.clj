(ns tawny.protege.repl
  (:require [org.dipert.swingrepl.main]
            [tawny.owl]))

(def ^{:dynamic true}
  owl-model-manager nil)

;; monkey patch tawny.owl
(alter-var-root
 #'tawny.owl/owl-data-factory
 (constantly
  (fn [] (.getOWLDataFactory owl-model-manager))))

(alter-var-root
 #'tawny.owl/owl-ontology-manager
 (constantly
  (fn [] (.getOWLOntologyManager owl-model-manager))))

(defn new-console [manager]
  (binding
      [owl-model-manager manager]
    (let [rtn
          (org.dipert.swingrepl.main/make-repl-jconsole {})]
      rtn)))

(defn kill-import-warning []
  (.setMissingImportHandler
   owl-model-manager
   (proxy [org.protege.editor.owl.model.MissingImportHandler] []
     (getDocumentIRI [iri]))))

(defn active-ontology
  ([]
     (.getActiveOntology owl-model-manager))
  ([ontology]
     (.setActiveOntology owl-model-manager ontology)))
