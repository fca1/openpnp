package org.openpnp.vision.pipeline;

public interface PipelineListener {

default void  prologCvStage(CvStage stage)  throws Exception
{
	
}
default void epilogCvStage(CvStage stage) throws Exception
{

}

};
