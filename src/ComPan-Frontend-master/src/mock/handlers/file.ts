import Mock from 'mockjs';
import { vfsApi } from '../vfs';
import JSZip from 'jszip';

export const setupFileMocks = () => {
  // Download File
  Mock.mock(/\/api\/v1\/files\/(.+)\/download/, 'get', (options) => {
    const fileId = (options.url.match(/\/api\/v1\/files\/(.+)\/download/) || [])[1];
    const file = vfsApi.get(fileId);

    if (!file || file.type !== 'file') {
      return Mock.HTTP.RAW({
        status: 404,
        statusText: 'Not Found',
        headers: { 'Content-Type': 'application/json; charset=utf-8' },
        // Body needs to be a string for RAW responses with JSON
        body: JSON.stringify({
          success: false,
          code: 404,
          message: 'File not found',
        }),
      });
    }
    
    const content = `Mock content for ${file.name}`;
    const blob = new Blob([content], { type: 'application/octet-stream' });
    
    // Using Mock.HTTP.RAW gives us more control over the response,
    // ensuring it behaves like a real file download for axios.
    return Mock.HTTP.RAW({
      status: 200,
      statusText: 'OK',
      headers: {
        'Content-Type': 'application/octet-stream',
        'Content-Disposition': `attachment; filename="${file.name}"`,
      },
      body: blob,
    });
  });

  // Batch Download Files as Zip
  Mock.mock(/\/api\/v1\/files\/batch-download/, 'post', async (options) => {
    const { fileIds } = JSON.parse(options.body);
    const zip = new JSZip();

    for (const fileId of fileIds) {
      const file = vfsApi.get(fileId);
      if (file && file.type === 'file') {
        // In a real scenario, you'd fetch file content. Here, we generate it.
        const fileContent = `Mock content for ${file.name}`;
        zip.file(file.name, fileContent);
      }
    }

    const zipBlob = await zip.generateAsync({ type: 'blob' });
    
    return Mock.HTTP.RAW({
      status: 200,
      statusText: 'OK',
      headers: {
        'Content-Type': 'application/zip',
        'Content-Disposition': `attachment; filename="ComPan-Download.zip"`,
      },
      body: zipBlob,
    });
  });

  // Rename File
  Mock.mock(/\/api\/v1\/files\/(.+)/, 'patch', (options) => {
    const fileId = (options.url.match(/\/api\/v1\/files\/(.+)/) || [])[1];
    const { fileName } = JSON.parse(options.body);
    const updatedFile = vfsApi.rename(fileId, fileName);
    return { success: true, code: 200, data: updatedFile };
  });

  // Delete File
  Mock.mock(/\/api\/v1\/files\/(.+)/, 'delete', (options) => {
    const fileId = (options.url.match(/\/api\/v1\/files\/(.+)/) || [])[1];
    vfsApi.delete(fileId);
    return { success: true, code: 200, data: { fileId } };
  });

  // Batch Delete Files
  Mock.mock(/\/api\/v1\/files\/batch/, 'post', (options) => {
    const { action, fileIds } = JSON.parse(options.body);
    if (action === 'delete') {
      fileIds.forEach((id: string) => vfsApi.delete(id));
      return { success: true, code: 200, data: { successCount: fileIds.length } };
    }
    return { success: false, code: 400, message: 'Action not mocked' };
  });
}; 