import Mock from 'mockjs';
import { vfsApi } from '../vfs';

export const setupUploadMocks = () => {
  // Preflight Upload
  Mock.mock(/\/api\/v1\/uploads\/preflight/, 'post', {
    success: true, code: 200, message: 'Ready for upload.',
    data: {
      status: 'UPLOADING',
      uploadId: Mock.Random.guid(),
      chunkSize: 1 * 1024 * 1024, // 1MB for easier testing
      uploadedChunkIndexes: [],
    }
  });

  // Upload Chunk
  Mock.mock(/\/api\/v1\/uploads\/(.+)\/chunk/, 'post', {
    success: true, code: 200, message: 'Chunk uploaded successfully.',
  });

  // Merge Chunks
  Mock.mock(/\/api\/v1\/uploads\/(.+)\/merge/, 'post', (options) => {
    const { fileName, parentId } = JSON.parse(options.body);
    const newFile = vfsApi.createFile(parentId, fileName, Mock.Random.integer(1000, 500000));
    
    return {
      success: true,
      code: 201,
      message: 'File created successfully.',
      data: newFile,
    };
  });
}; 