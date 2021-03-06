#include <stdlib.h>
#include "ippiImageStore.h"
#include "cfile.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

bool CIppiImageStore::Error(std::string message) {
  return false;
}

bool CIppiImageStore::Save(CFile* pFile) {
#if 0
  if (!ValidImage(m_pImage)) return false;
  m_pFile = pFile;
  m_bSave = true;

  pFile->Write(GetFileHeader(m_pImage), FileHeaderSize());
  pFile->Write(GetInfoHeader(m_pImage), InfoHeaderSize());
  if (GetPaletteSize(m_pImage)) {
    pFile->Write(GetGrayPalette(m_pImage), GetPaletteSize(m_pImage));
  }
  pFile->Seek(m_FileHeader.bfOffBits, CFile::begin);
  const char p[2] = {0x00,0x00};
  pFile->Write(p,sizeof(p));
  pFile->Write(m_pImage->DataPtr(), m_pImage->DataSize());
#endif
  return true;
}

bool CIppiImageStore::ValidImage(CIppiImage* pImage) {
  if ((pImage->Depth() == 8) && !pImage->Complex()
      && !(pImage->Plane() && pImage->Channels() > 1))
    return true;
  else
    return false;
}

BITMAPFILEHEADER* CIppiImageStore::GetFileHeader(CIppiImage* pImage) {
  m_FileHeader.bfType = 0x4d42;
  m_FileHeader.bfReserved1 = 0;
  m_FileHeader.bfReserved2 = 0;
  m_FileHeader.bfOffBits = (FileHeaderSize() + InfoHeaderSize()
      + GetPaletteSize(pImage) + 3) & 0xFFFFFFFC;
  m_FileHeader.bfSize = m_FileHeader.bfOffBits + m_pImage->DataSize();
  return &m_FileHeader;
}

BITMAPINFOHEADER* CIppiImageStore::GetInfoHeader(CIppiImage* pImage) {
  m_InfoHeader.biSize = sizeof(BITMAPINFOHEADER);
  m_InfoHeader.biWidth = pImage->Width();
  m_InfoHeader.biHeight = pImage->Height();
  m_InfoHeader.biPlanes = pImage->Plane() ? pImage->Channels() : 1;
  m_InfoHeader.biBitCount =
      pImage->Plane() ? pImage->Depth() :
      pImage->Complex() ?
          pImage->Depth() * 2 : pImage->Depth() * pImage->Channels();
  m_InfoHeader.biSizeImage = pImage->DataSize();
  m_InfoHeader.biXPelsPerMeter = 0;
  m_InfoHeader.biYPelsPerMeter = 0;
  m_InfoHeader.biClrUsed = 0;
  m_InfoHeader.biClrImportant = 0;
  m_InfoHeader.biCompression = 0;
  return &m_InfoHeader;
}

int CIppiImageStore::GetPaletteNum(CIppiImage* pImage) {
  if ((pImage->Depth() == 8) && (pImage->Channels() == 1) && !pImage->Complex())
    return 256;
  else
    return 0;
}

RGBQUAD* CIppiImageStore::GetGrayPalette(CIppiImage* pImage) {
  for (int i = 0; i < 256; i++) {
    m_Palette[i].rgbBlue = i;
    m_Palette[i].rgbGreen = i;
    m_Palette[i].rgbRed = i;
    m_Palette[i].rgbReserved = 0;
  }
  return m_Palette;
}

/////////////////////////////////////////////////////////////////////////

bool CIppiImageStore::Load(CFile* pFile) {
  m_pFile = pFile;
  m_bSave = false;

  pFile->Read(&m_FileHeader, FileHeaderSize());
  if (!ValidFileHeader())
    return false;
  pFile->Read(&m_InfoHeader, InfoHeaderSize());
  if (GetPaletteSize()) {
    pFile->Read(m_Palette, GetPaletteSize());
  }
  if (!CreateImage())
    return false;

  pFile->Seek(m_FileHeader.bfOffBits, CFile::begin);
  if (IsCompressed()) {
    Ipp8u* buffer = (Ipp8u*) malloc(GetDataSize());
    pFile->Read(buffer, GetDataSize());
    SetImageData(buffer);
    free(buffer);
  } else {
    pFile->Read(m_pImage->DataPtr(), m_pImage->DataSize());
  }

  return true;
}

bool CIppiImageStore::ValidFileHeader() {
  if (m_FileHeader.bfType != 0x4d42)
    return Error("Bitmap files are valid only");
  return true;
}

bool CIppiImageStore::CreateImage() {
  if (!ValidCompression())
    return false;
  ppType type;
  int width, height, channels;
  bool plane;
  if (!SetImageWidth(width))
    return false;
  if (!SetImageHeight(height))
    return false;
  if (!SetImageType(type))
    return false;
  if (!SetImagePlane(plane))
    return false;
  if (!SetImageChannels(channels))
    return false;
  return m_pImage->CreateImage(width, height, channels, type, plane);
}

bool CIppiImageStore::ValidCompression() {
  unsigned int comprssn = m_InfoHeader.biCompression;
  if (comprssn == 0)
    return true;
  else
    return Error("Compression is not valid");
}

bool CIppiImageStore::SetImageWidth(int& width) {
  width = m_InfoHeader.biWidth;
  if (width <= 0)
    return Error("Bitmap header is corrupted: biWidth is invalid");
  return true;
}

bool CIppiImageStore::SetImageHeight(int& height) {
  height = m_InfoHeader.biHeight;
  if (height <= 0)
    return Error("Bitmap header is corrupted: biHeight is invalid");
  return true;
}

bool CIppiImageStore::SetImageType(ppType& type) {
  switch (m_InfoHeader.biCompression) {
  case 0:
    switch (m_InfoHeader.biBitCount) {
    case 1:
    case 4:
    case 8:
    case 16:
    case 24:
    case 32:
      type = pp8u;
      return true;
    }
    break;
  }
  return Error("Bitmap header is corrupted: biBitCount is invalid");
}

bool CIppiImageStore::SetImagePlane(bool& plane) {
  switch (m_InfoHeader.biCompression) {
  case 0:
    switch (m_InfoHeader.biPlanes) {
    case 1:
      plane = false;
      return true;
    }
    break;
  default:
    switch (m_InfoHeader.biPlanes) {
    case 1:
      plane = false;
      return true;
    case 3:
    case 4:
      plane = true;
      return true;
    }
    break;
  }
  return Error("Bitmap header is corrupted: biPlanes is invalid");
}

bool CIppiImageStore::SetImageChannels(int& channels) {
  int planes = m_InfoHeader.biPlanes;
  switch (m_InfoHeader.biCompression) {
  case 0:
    switch (m_InfoHeader.biBitCount) {
    case 1:
    case 4:
    case 8:
      channels = IsGrayPalette() ? 1 : 3;
      return true;
    case 16:
      channels = 2;
      return true;
    case 24:
      channels = 3;
      return true;
    case 32:
      channels = 4;
      return true;
    }
    break;
  }
  return Error("Bitmap header is corrupted: biBitCount is invalid");
}

int CIppiImageStore::GetPaletteNum() {
  if (m_InfoHeader.biCompression != 0)
    return 0;
  if (m_InfoHeader.biBitCount > 8)
    return 0;
  int num = 1 << m_InfoHeader.biBitCount;
  if (m_InfoHeader.biClrUsed == 0)
    return num;
  if ((int) m_InfoHeader.biClrUsed > num)
    return num;
  return m_InfoHeader.biClrUsed;
}

bool CIppiImageStore::IsGrayPalette() {
  for (int i = 0; i < GetPaletteNum(); i++) {
    if (m_Palette[i].rgbBlue != m_Palette[i].rgbGreen)
      return false;
    if (m_Palette[i].rgbBlue != m_Palette[i].rgbRed)
      return false;
  }
  return true;
}

int CIppiImageStore::GetDataSize() {
  return GetDataStep() * m_InfoHeader.biHeight * m_InfoHeader.biPlanes;
}

int CIppiImageStore::GetDataStep() {
  return (m_InfoHeader.biBitCount * m_InfoHeader.biWidth + 31) / 32 * 4;
}

bool CIppiImageStore::IsCompressed() {
  return (m_InfoHeader.biCompression == 0) && (m_InfoHeader.biBitCount <= 8);
}

bool CIppiImageStore::SetImageData(Ipp8u* buffer) {
  switch (m_InfoHeader.biBitCount) {
  case 1:
    return SetImageData_1(buffer);
  case 4:
    return SetImageData_4(buffer);
  case 8:
    return SetImageData_8(buffer);
  }
  return false;
}

bool CIppiImageStore::SetImageData_8(Ipp8u* src) {
  int srcStep = GetDataStep();
  int dstStep = m_pImage->Step();
  Ipp8u* dst = (Ipp8u*) m_pImage->DataPtr();

  for (int y = 0; y < m_pImage->Height(); y++) {
    for (int x = 0; x < m_pImage->Width(); x++) {
      SetPixel(src[x], dst, x);
    }
    src += srcStep;
    dst += dstStep;
  }
  return true;
}

bool CIppiImageStore::SetImageData_4(Ipp8u* src) {
  int srcStep = GetDataStep();
  int dstStep = m_pImage->Step();
  Ipp8u* dst = (Ipp8u*) m_pImage->DataPtr();

  for (int y = 0; y < m_pImage->Height(); y++) {
    int x = 0;
    int i;
    for (i = 0; i < (m_pImage->Width() >> 1); i++) {
      SetPixel((src[i] & 0xF0) >> 4, dst, x++);
      SetPixel(src[i] & 0x0F, dst, x++);
    }
    if (x < m_pImage->Width()) {
      SetPixel((src[i] & 0xF0) >> 4, dst, x++);
    }
    src += srcStep;
    dst += dstStep;
  }
  return true;
}

bool CIppiImageStore::SetImageData_1(Ipp8u* src) {
  int srcStep = GetDataStep();
  int dstStep = m_pImage->Step();
  Ipp8u* dst = (Ipp8u*) m_pImage->DataPtr();

  for (int y = 0; y < m_pImage->Height(); y++) {
    int x = 0;
    int i;
    for (i = 0; i < (m_pImage->Width() >> 3); i++) {
      Ipp8u byte = src[i];
      for (int j = 0; j < 8; j++) {
        SetPixel(byte & 0x80, dst, x++);
        byte <<= 1;
      }
    }
    if (x < m_pImage->Width()) {
      Ipp8u byte = src[i];
      while (x < m_pImage->Width()) {
        SetPixel(byte & 1, dst, x++);
        byte >>= 1;
      }
    }
    src += srcStep;
    dst += dstStep;
  }
  return true;
}

void CIppiImageStore::SetPixel(Ipp8u src, Ipp8u* pDst, int x) {
  if (src >= GetPaletteNum())
    src = GetPaletteNum() - 1;
  RGBQUAD pal = m_Palette[src];
  if (m_pImage->Channels() == 1) {
    pDst[x] = pal.rgbBlue;
  } else {
    pDst[3 * x] = pal.rgbBlue;
    pDst[3 * x + 1] = pal.rgbGreen;
    pDst[3 * x + 2] = pal.rgbRed;
  }
}

