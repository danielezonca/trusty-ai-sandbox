import { ChannelType } from "@kogito-tooling/core-api";
import { EditorType, EmbeddedEditorRouter, EmbeddedViewer, File } from "@kogito-tooling/embedded-editor";
import { GwtEditorRoutes } from "@kogito-tooling/kie-bc-editors";
import React, { useMemo } from "react";
import { IExecutionModelResponse } from "../Audit/types";
import { LinearRegressionViewer } from "./pmml/LinearRegressionViewer";

const DMN1_2: string = "http://www.omg.org/spec/DMN/20151101/dmn.xsd";
const PMML4_4: string = "http://www.dmg.org/PMML-4_4";

type ModelDiagramProps = {
  model: IExecutionModelResponse
}

function makeUnknownModel(): JSX.Element {
  return <div>Unknown model type</div>;
}

function makeDMNEditor(model: IExecutionModelResponse,
  router: EmbeddedEditorRouter): JSX.Element {

  const file: File = {
    fileName: model.name,
    editorType: EditorType.DMN,
    getFileContents: () => Promise.resolve(model.model),
    isReadOnly: true
  };

  return (
    <EmbeddedViewer
      file={file}
      router={router}
      channelType={ChannelType.EMBEDDED}
      envelopeUri={process.env.PUBLIC_URL + '/envelope/envelope.html'}
    />);
}

function makePMMLEditor(model: IExecutionModelResponse): JSX.Element {
  return <LinearRegressionViewer xml={model.model} />;
}

const DEFAULT: JSX.Element = makeUnknownModel();

const ModelDiagram = (props: ModelDiagramProps) => {
  const { model } = props;
  const type: string = model.type;

  const router: EmbeddedEditorRouter = useMemo(
    () =>
      new EmbeddedEditorRouter(
        new GwtEditorRoutes({
          dmnPath: "gwt-editors/dmn",
          bpmnPath: "gwt-editors/bpmn",
          scesimPath: "gwt-editors/scesim"
        })
      ),
    []
  );

  switch (type) {
    case DMN1_2:
      return makeDMNEditor(model, router);
    case PMML4_4:
      return makePMMLEditor(model);
  }

  return DEFAULT;
};

export default ModelDiagram;
